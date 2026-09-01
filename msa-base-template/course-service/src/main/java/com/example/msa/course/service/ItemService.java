package com.example.msa.course.service;

import com.example.msa.course.dto.ItemDto;
import com.example.msa.course.entity.Item;
import com.example.msa.course.kafka.ItemCreatedEvent;
import com.example.msa.course.kafka.ItemEventProducer;
import com.example.msa.course.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemEventProducer itemEventProducer;

    @Transactional
    public ItemDto.Response create(ItemDto.CreateRequest request) {
        if (itemRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("이미 존재하는 code 입니다");
        }
        Item item = itemRepository.save(Item.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .soldCount(0)
                .build());

        itemEventProducer.publishItemCreated(ItemCreatedEvent.builder()
                .itemId(item.getId())
                .code(item.getCode())
                .name(item.getName())
                .price(item.getPrice())
                .build());

        return ItemDto.Response.from(item);
    }

    @Transactional(readOnly = true)
    public List<ItemDto.Response> findAll() {
        return itemRepository.findAll().stream().map(ItemDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto.Response findById(Long id) {
        return ItemDto.Response.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return itemRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return itemRepository.count();
    }

    @Transactional
    public ItemDto.Response update(Long id, ItemDto.UpdateRequest request) {
        Item item = getOrThrow(id);
        item.update(request.getName(), request.getDescription(), request.getPrice());
        return ItemDto.Response.from(item);
    }

    @Transactional
    public void delete(Long id) {
        itemRepository.delete(getOrThrow(id));
    }

    @Transactional
    public void increaseSoldCount(Long id) {
        Item item = getOrThrow(id);
        item.increaseSoldCount();
        log.info("[ItemService] soldCount 증가 - itemId={}, soldCount={}", id, item.getSoldCount());
    }

    private Item getOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }
}
