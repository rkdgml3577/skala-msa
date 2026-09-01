package com.example.msa.user.service;

import com.example.msa.user.dto.UserDto;
import com.example.msa.user.entity.User;
import com.example.msa.user.kafka.UserCreatedEvent;
import com.example.msa.user.kafka.UserEventProducer;
import com.example.msa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    @Transactional
    public UserDto.Response create(UserDto.CreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 username 입니다");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 email 입니다");
        }

        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .displayName(request.getDisplayName())
                .active(true)
                .build());

        // 도메인 이벤트 발행 (다른 서비스가 구독 가능)
        userEventProducer.publishUserCreated(UserCreatedEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build());

        return UserDto.Response.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto.Response> findAll() {
        return userRepository.findAll().stream().map(UserDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public UserDto.Response findById(Long id) {
        return UserDto.Response.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public UserDto.Response update(Long id, UserDto.UpdateRequest request) {
        User user = getOrThrow(id);
        user.update(request.getDisplayName());
        return UserDto.Response.from(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getOrThrow(id);
        user.deactivate();
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
    }
}
