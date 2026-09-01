package com.example.msa.enrollment.service;

import com.example.msa.enrollment.dto.OrderDto;
import com.example.msa.enrollment.entity.Order;
import com.example.msa.enrollment.kafka.OrderCreatedEvent;
import com.example.msa.enrollment.kafka.OrderEventProducer;
import com.example.msa.enrollment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final OrderEventProducer orderEventProducer;

    /**
     * 주문 생성 흐름:
     * 1) user-service 로 사용자 존재 확인 (동기 REST)
     * 2) course-service 로 상품 조회 및 가격 스냅샷 (동기 REST)
     * 3) 주문 저장(PENDING)
     * 4) order.created 이벤트 발행 (비동기 후속 처리)
     */
    @Transactional
    public OrderDto.Response create(OrderDto.CreateRequest request) {
        if (!userServiceClient.existsUser(request.getUserId())) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다: " + request.getUserId());
        }

        CourseServiceClient.ItemView item = courseServiceClient.getItem(request.getItemId());
        BigDecimal unitPrice = item.getPrice();

        Order order = orderRepository.save(Order.builder()
                .userId(request.getUserId())
                .itemId(request.getItemId())
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .status(Order.Status.PENDING)
                .build());

        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        orderEventProducer.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .itemId(order.getItemId())
                .quantity(order.getQuantity())
                .amount(amount)
                .build());

        return OrderDto.Response.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> findByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(OrderDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderDto.Response findById(Long id) {
        return OrderDto.Response.from(getOrThrow(id));
    }

    @Transactional
    public OrderDto.Response cancel(Long id) {
        Order order = getOrThrow(id);
        order.cancel();
        return OrderDto.Response.from(order);
    }

    private Order getOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
    }
}
