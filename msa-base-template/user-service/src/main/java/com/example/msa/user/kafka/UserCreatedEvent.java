package com.example.msa.user.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** user.created 토픽으로 발행하는 이벤트 payload. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {
    private Long userId;
    private String username;
    private String email;
}
