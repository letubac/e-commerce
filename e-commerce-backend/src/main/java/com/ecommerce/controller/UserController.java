package com.ecommerce.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.service.UserService;

/**
 * REST controller for managing users (Admin only).
 * Provides endpoints for user administration.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Get all users (Admin only)
     */
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String role) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            // Get users from service
            Page<UserDTO> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword, pageRequest);
            } else {
                users = userService.getAllUsersWithPagination(pageRequest);
            }

            // Filter by role if provided
            if (role != null && !role.trim().isEmpty()) {
                List<UserDTO> filteredContent = users.getContent().stream()
                        .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase(role))
                        .collect(java.util.stream.Collectors.toList());
                users = new org.springframework.data.domain.PageImpl<>(filteredContent, pageRequest,
                        filteredContent.size());
            }

            // Filter by active status if provided
            if (active != null) {
                List<UserDTO> filteredContent = users.getContent().stream()
                        .filter(user -> user.isActive() == active)
                        .collect(java.util.stream.Collectors.toList());
                users = new org.springframework.data.domain.PageImpl<>(filteredContent, pageRequest,
                        filteredContent.size());
            }

            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách người dùng thành công", users));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách người dùng", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy danh sách người dùng"));
        }
    }

    /**
     * Get user by ID (Admin only)
     */
    @GetMapping("/users/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        try {
            // Get user from service
            UserDTO user = userService.getUserByIdOrThrow(id);

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin người dùng thành công", user));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy người dùng ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy người dùng ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy thông tin người dùng"));
        }
    }

    /**
     * Lock user account (Admin only)
     */
    @PutMapping("/users/{id}/lock")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> lockUser(@PathVariable Long id) {
        try {
            // Lock user through service
            userService.lockUser(id);
            log.info("Admin đã khóa tài khoản người dùng ID: {}", id);
            return ResponseEntity.ok(new ApiResponse(true, "Khóa tài khoản người dùng thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy người dùng ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi khóa tài khoản người dùng ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi khóa tài khoản"));
        }
    }

    /**
     * Unlock user account (Admin only)
     */
    @PutMapping("/users/{id}/unlock")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> unlockUser(@PathVariable Long id) {
        try {
            // Unlock user through service
            userService.unlockUser(id);
            log.info("Admin đã mở khóa tài khoản người dùng ID: {}", id);
            return ResponseEntity.ok(new ApiResponse(true, "Mở khóa tài khoản người dùng thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy người dùng ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi mở khóa tài khoản người dùng ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi mở khóa tài khoản"));
        }
    }

    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/users/statistics")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUserStatistics() {
        try {
            Map<String, Object> statistics = userService.getUserStatistics();

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thống kê người dùng thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê người dùng", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy thống kê người dùng"));
        }
    }
}