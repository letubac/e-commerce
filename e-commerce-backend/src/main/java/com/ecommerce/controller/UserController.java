package com.ecommerce.controller;

import java.util.Map;

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

import com.ecommerce.dto.UserDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.UserService;
import com.ecommerce.webapp.BusinessApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing users (Admin only).
 * Provides endpoints for user administration.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get all users (Admin only)
     */
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(name = "keyword", required = false) String keyword) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<UserDTO> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword, pageRequest);
            } else {
                users = userService.getAllUsersWithPagination(pageRequest);
            }

            return ResponseEntity.ok(successHandler.handlerSuccess(users, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get user by ID (Admin only)
     */
    @GetMapping("/users/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getUserById(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            UserDTO user = userService.getUserByIdOrThrow(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(user, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Lock user account (Admin only)
     */
    @PutMapping("/users/{id}/lock")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> lockUser(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            userService.lockUser(id);
            log.info("Admin locked user account ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Unlock user account (Admin only)
     */
    @PutMapping("/users/{id}/unlock")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> unlockUser(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            userService.unlockUser(id);
            log.info("Admin unlocked user account ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/users/statistics")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getUserStatistics() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> statistics = userService.getUserStatistics();
            return ResponseEntity.ok(successHandler.handlerSuccess(statistics, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}