package com.example.msa.course;

import com.example.msa.course.dto.ItemDto;
import com.example.msa.course.kafka.ItemEventProducer;
import com.example.msa.course.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CourseServiceApplicationTests {

    @Autowired
    private ItemService itemService;

    // Kafka 브로커 없이 실행하기 위해 이벤트 발행은 목으로 대체
    @MockBean
    private ItemEventProducer itemEventProducer;

    @Test
    void contextLoads() {
    }

    @Test
    void createAndIncreaseSoldCount() {
        ItemDto.Response created = itemService.create(ItemDto.CreateRequest.builder()
                .code("ITEM-1")
                .name("Sample Item")
                .description("desc")
                .price(new BigDecimal("100.00"))
                .build());

        assertNotNull(created.getId());
        assertEquals(0, created.getSoldCount());

        itemService.increaseSoldCount(created.getId());
        assertEquals(1, itemService.findById(created.getId()).getSoldCount());
    }
}
