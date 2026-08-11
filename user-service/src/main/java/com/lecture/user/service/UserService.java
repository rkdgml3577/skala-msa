package com.lecture.user.service;

import com.lecture.user.dto.UserDto;
import com.lecture.user.entity.User;
import com.lecture.user.exception.ApiKeyAuthenticationException;
import com.lecture.user.kafka.TransactionKafkaProducer;
import com.lecture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionClient subscriptionClient;
    private final CourseGradeClient courseGradeClient;
    private final AiScreeningAuditService aiScreeningAuditService;
    private final TransactionKafkaProducer transactionKafkaProducer;

    /**
     * 회원가입
     */
    @Transactional
    public UserDto.UserResponse register(UserDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        User.Role role = request.getRole() != null ? request.getRole() : User.Role.STUDENT;

        String apiKey = "shr_" + UUID.randomUUID().toString().replace("-", "");
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .organizationName(request.getOrganizationName())
                .clientCode("CLIENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .apiKeyHash(passwordEncoder.encode(apiKey))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        return UserDto.UserResponse.from(savedUser, apiKey);
    }

    /**
     * 사용자 단건 조회
     */
    public UserDto.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
        return UserDto.UserResponse.from(user);
    }

    /**
     * 이메일로 사용자 조회 (서비스 간 내부 호출용)
     */
    public UserDto.UserResponse getUserByEmail(String email) {
        System.out.println(">>> getUserByEmail email = " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        return UserDto.UserResponse.from(user);
    }

    /**
     * Gateway JWT는 user_id 클레임이 있으면 숫자 ID를, 없으면 subject(이메일)를 전달한다.
     * 두 형식을 모두 수용해 OAuth 로그인 직후 사용자 정보를 안정적으로 조회한다.
     */
    public UserDto.UserResponse getUserByGatewayIdentifier(String userIdentifier) {
        if (userIdentifier == null || userIdentifier.isBlank()) {
            throw new IllegalArgumentException("사용자 식별자가 없습니다");
        }
        try {
            return getUserById(Long.parseLong(userIdentifier));
        } catch (NumberFormatException ignored) {
            return getUserByEmail(userIdentifier);
        }
    }

    /** API 키는 평문 저장하지 않고 재발급 시 한 번만 응답한다. */
    @Transactional
    public UserDto.ApiKeyResponse rotateApiKey(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객사를 찾을 수 없습니다: " + id));
        String apiKey = "shr_" + UUID.randomUUID().toString().replace("-", "");
        user.rotateApiKey(passwordEncoder.encode(apiKey));
        return UserDto.ApiKeyResponse.builder()
                .clientId(user.getId())
                .clientCode(user.getClientCode())
                .apiKey(apiKey)
                .notice("이 API 키는 다시 표시되지 않습니다.")
                .build();
    }

    /**
     * API 키로 고객사를 인증하고, 활성 구독 모델별 거래 이벤트를 Kafka에 발행한다.
     * API 키는 BCrypt 해시만 저장되므로 활성 고객사 해시와 안전하게 대조한다.
     */
    public UserDto.TransactionAcceptedResponse receiveTransaction(
            String rawApiKey, UserDto.TransactionRequest request) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new ApiKeyAuthenticationException("X-API-Key 헤더가 필요합니다");
        }

        User client = userRepository.findByActiveTrue().stream()
                .filter(user -> user.getApiKeyHash() != null && passwordEncoder.matches(rawApiKey, user.getApiKeyHash()))
                .findFirst()
                .orElseThrow(() -> new ApiKeyAuthenticationException("유효하지 않거나 비활성화된 API 키입니다"));

        List<String> activeModels = subscriptionClient.getActiveModelCodes(client.getId());
        if (activeModels.isEmpty()) {
            throw new IllegalArgumentException("활성 구독 모델이 없어 거래를 처리할 수 없습니다");
        }

        String ticker = request.getTicker().trim().toUpperCase(Locale.ROOT);

        // 기존 AAOIFI 종목 심사를 구독한 기관에만 사전 제한 필터를 적용한다.
        // 제한 이력이 있는 종목은 AI/Kafka로 보내지 않아 중복 판정을 방지한다.
        if (activeModels.contains("AAOIFI_CORE")) {
            var existingGrade = courseGradeClient.findExistingGrade(ticker);
            if (existingGrade.isPresent() && existingGrade.get().isRestricted()) {
                CourseGradeClient.GradeResult restricted = existingGrade.get();
                return UserDto.TransactionAcceptedResponse.builder()
                        .transactionId(request.getTransactionId())
                        .clientId(client.getId())
                        .clientCode(client.getClientCode())
                        .dispatchedModels(List.of())
                        .blocked(true)
                        .blockReason(restricted.reason())
                        .ruleVersion(restricted.ruleVersionId())
                        .decision("BLOCKED")
                        .processingCompleted(true)
                        .results(List.of())
                        .message("기존 AAOIFI 종목 심사에서 제한된 종목으로 확인되어 AI 판정을 요청하지 않았습니다")
                        .build();
            }
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        for (String modelCode : activeModels) {
            transactionKafkaProducer.publishTradeReceived(client, modelCode, request, ticker);
        }

        List<UserDto.AiScreeningResultResponse> results = aiScreeningAuditService.awaitTransactionResults(
                client.getId(), request.getTransactionId(), submittedAt, activeModels, Duration.ofSeconds(3));
        boolean alertDetected = results.stream().anyMatch(result -> "ALERT".equals(result.getStatus()));
        boolean reviewRequired = results.stream().anyMatch(result -> "REVIEW".equals(result.getStatus()));
        boolean completed = alertDetected || results.stream()
                .map(UserDto.AiScreeningResultResponse::getModelCode)
                .distinct()
                .count() == activeModels.size();
        String decision = !completed ? "PENDING" : alertDetected ? "ALERT" : reviewRequired ? "REVIEW" : "CLEAR";

        return UserDto.TransactionAcceptedResponse.builder()
                .transactionId(request.getTransactionId())
                .clientId(client.getId())
                .clientCode(client.getClientCode())
                .dispatchedModels(activeModels)
                .blocked(alertDetected)
                .blockReason(alertDetected ? "구독 모델의 이상 탐지 기준에 해당합니다" : null)
                .decision(decision)
                .processingCompleted(completed)
                .results(results)
                .message(messageFor(decision))
                .build();
    }

    private String messageFor(String decision) {
        return switch (decision) {
            case "ALERT" -> "이상 탐지 기준에 해당하여 거래 차단이 필요합니다";
            case "REVIEW" -> "수동 검토가 필요한 거래입니다";
            case "CLEAR" -> "활성 구독 모델의 적합성 판정을 통과했습니다";
            default -> "AI 판정이 진행 중입니다. 탐지 결과 탭에서 확인할 수 있습니다";
        };
    }
}
