package com.example.msa.user.controller;

import com.example.msa.user.dto.ApiResponse;
import com.example.msa.user.dto.UserDto;
import com.example.msa.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto.Response>> create(
            @Valid @RequestBody UserDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.create(request)));
    }

    /** 전체 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAll()));
    }

    /** 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }

    /** 수정 */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> update(
            @PathVariable Long id, @Valid @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request)));
    }

    /** 삭제(비활성화) */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // === 서비스 간 내부 호출용 엔드포인트 ===

    /** 다른 서비스가 사용자 존재 여부를 확인할 때 사용 */
    @GetMapping("/internal/exists/{id}")
    public boolean exists(@PathVariable Long id) {
        return userService.exists(id);
    }
}
