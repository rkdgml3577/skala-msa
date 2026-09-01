package com.example.msa.course.controller;

import com.example.msa.course.dto.ApiResponse;
import com.example.msa.course.dto.ItemDto;
import com.example.msa.course.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 상품(Item) 카탈로그 REST API. 경로는 인프라 라우팅 호환을 위해 /api/courses 를 사용한다. */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ApiResponse<ItemDto.Response>> create(
            @Valid @RequestBody ItemDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(itemService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(itemService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(itemService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto.Response>> update(
            @PathVariable Long id, @Valid @RequestBody ItemDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(itemService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // === 서비스 간 내부 호출용 ===

    @GetMapping("/internal/exists/{id}")
    public boolean exists(@PathVariable Long id) {
        return itemService.exists(id);
    }

    @GetMapping("/internal/{id}")
    public ItemDto.Response getInternal(@PathVariable Long id) {
        return itemService.findById(id);
    }
}
