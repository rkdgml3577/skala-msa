package com.lecture.user.controller;

import com.lecture.user.dto.UserDto;
import com.lecture.user.service.AiScreeningAuditService;
import com.lecture.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AiScreeningAuditService aiScreeningAuditService;

    /**
     * POST /users/register - 회원가입 (인증 불필요)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> register(
            @Valid @RequestBody UserDto.RegisterRequest request) {
        UserDto.UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.ApiResponse.success(response));
    }

    /**
     * GET /users/{id} - 사용자 조회 (인증 필요)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> getUser(
            @PathVariable Long id) {
        UserDto.UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(UserDto.ApiResponse.success(response));
    }

    /**
     * GET /users/me - 내 정보 조회
     * API Gateway가 전달한 X-User-Id 헤더(숫자 userId)를 사용
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> getMe(
            @RequestHeader("X-User-Id") String userIdentifier) {

        UserDto.UserResponse response = userService.getUserByGatewayIdentifier(userIdentifier);
        return ResponseEntity.ok(UserDto.ApiResponse.success(response));
    }

    /** 로그인한 은행사의 AI 탐지 결과·감사 이력만 반환한다. */
    @GetMapping("/me/ai-results")
    public ResponseEntity<UserDto.ApiResponse<java.util.List<UserDto.AiScreeningResultResponse>>> getMyAiResults(
            @RequestHeader("X-User-Id") String userIdentifier) {
        UserDto.UserResponse user = userService.getUserByGatewayIdentifier(userIdentifier);
        return ResponseEntity.ok(UserDto.ApiResponse.success(aiScreeningAuditService.getResults(user.getId())));
    }

    /**
     * GET /users/internal/{id} - 서비스 간 내부 호출용 (Client Credentials)
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserInternal(@PathVariable Long id) {
        UserDto.UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/api-key")
    public ResponseEntity<UserDto.ApiResponse<UserDto.ApiKeyResponse>> rotateApiKey(@PathVariable Long id) {
        return ResponseEntity.ok(UserDto.ApiResponse.success(userService.rotateApiKey(id)));
    }

    /**
     * 금융회사 거래 수신 API. JWT 대신 금융회사에 발급한 X-API-Key로 인증한다.
     * 이 API는 API Gateway의 JWT 정책과 분리하기 위해 user-service(8081)에 직접 노출한다.
     */
    @PostMapping("/transactions")
    public ResponseEntity<UserDto.ApiResponse<UserDto.TransactionAcceptedResponse>> receiveTransaction(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody UserDto.TransactionRequest request) {
        UserDto.TransactionAcceptedResponse response = userService.receiveTransaction(apiKey, request);
        return ResponseEntity.status(response.isProcessingCompleted() ? HttpStatus.OK : HttpStatus.ACCEPTED)
                .body(UserDto.ApiResponse.success(response));
    }
}
