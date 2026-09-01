package com.example.msa.user;

import com.example.msa.user.dto.UserDto;
import com.example.msa.user.kafka.UserEventProducer;
import com.example.msa.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceApplicationTests {

    @Autowired
    private UserService userService;

    // Kafka 브로커 없이 실행하기 위해 이벤트 발행은 목으로 대체
    @MockBean
    private UserEventProducer userEventProducer;

    @Test
    void contextLoads() {
    }

    @Test
    void createAndFindUser() {
        UserDto.Response created = userService.create(UserDto.CreateRequest.builder()
                .username("alice")
                .email("alice@example.com")
                .displayName("Alice")
                .build());

        assertNotNull(created.getId());
        assertEquals("alice", userService.findById(created.getId()).getUsername());
    }

    @Test
    void rejectDuplicateUsername() {
        userService.create(UserDto.CreateRequest.builder()
                .username("bob").email("bob@example.com").displayName("Bob").build());

        assertThrows(IllegalArgumentException.class, () ->
                userService.create(UserDto.CreateRequest.builder()
                        .username("bob").email("bob2@example.com").displayName("Bob2").build()));
    }
}
