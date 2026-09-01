package com.example.msa.enrollment;

import com.example.msa.enrollment.dto.OrderDto;
import com.example.msa.enrollment.kafka.OrderEventProducer;
import com.example.msa.enrollment.service.CourseServiceClient;
import com.example.msa.enrollment.service.EnrollmentService;
import com.example.msa.enrollment.service.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
class EnrollmentServiceApplicationTests {

    @Autowired
    private EnrollmentService enrollmentService;

    // 서비스 간 통신/이벤트는 목으로 대체 (H2 단독 실행)
    @MockBean
    private UserServiceClient userServiceClient;
    @MockBean
    private CourseServiceClient courseServiceClient;
    @MockBean
    private OrderEventProducer orderEventProducer;

    @Test
    void contextLoads() {
    }

    @Test
    void createOrder() {
        when(userServiceClient.existsUser(1L)).thenReturn(true);
        CourseServiceClient.ItemView item = mock(CourseServiceClient.ItemView.class);
        when(item.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(courseServiceClient.getItem(10L)).thenReturn(item);

        OrderDto.Response created = enrollmentService.create(OrderDto.CreateRequest.builder()
                .userId(1L).itemId(10L).quantity(2).build());

        assertNotNull(created.getId());
        assertEquals("PENDING", created.getStatus());
        verify(orderEventProducer, times(1)).publishOrderCreated(any());
    }
}
