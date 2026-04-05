package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.AuthService;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import com.ecommerce.service.SessionTimeoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RestController
@RequestMapping("/api/v1/auth")

/**
 * author: LeTuBac
 */
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionTimeoutService sessionTimeoutService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody CreateUserRequest request) {
        try {
            // Check if username already exists
            if (!userService.isUsernameAvailable(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Tên đăng nhập đã được sử dụng!"));
            }

            // Check if email already exists
            if (!userService.isEmailAvailable(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email đã được sử dụng!"));
            }

            UserDTO user = userService.createUser(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đăng ký tài khoản thành công!", user));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Đăng ký thất bại: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authService.authenticate(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword());

            String accessToken = authService.generateToken(authentication);
            String refreshToken = authService.generateRefreshToken(authentication);
            long expirationTime = authService.getTokenExpirationTime(accessToken) / 1000; // Đổi sang giây

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            UserDTO user = userService.getUserByIdOrThrow(userPrincipal.getId());

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, expirationTime, user));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Tên đăng nhập hoặc mật khẩu không chính xác"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        try {
            Long userId = authService.getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserDTO user = userService.getUserByIdOrThrow(userId);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<String>> verify2FA(@RequestBody Map<String, String> request) {
        // For now, just return success - implement actual 2FA logic later
        String code = request.get("code");

        if (code != null && !code.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "2FA verified successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid 2FA code"));
        }
    }

    @GetMapping("/token/check")
    public ResponseEntity<Map<String, Object>> checkTokenStatus(Authentication authentication) {
        try {
            Map<String, Object> response = new HashMap<>();

            if (authentication != null && authService.getUserIdFromAuthentication(authentication) != null) {
                response.put("valid", true);
                response.put("message", "Token hợp lệ");
                response.put("userId", authService.getUserIdFromAuthentication(authentication));
            } else {
                response.put("valid", false);
                response.put("message", "Token không hợp lệ hoặc đã hết hạn");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Lỗi kiểm tra token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // Với JWT, logout thường được xử lý ở client bằng cách xóa token
        // Ở đây chúng ta chỉ trả về response thành công
        // Trong tương lai có thể implement blacklist cho token
        return ResponseEntity.ok(new ApiResponse<>(true, "Đăng xuất thành công"));
    }

    @GetMapping("/timeout/config")
    public ResponseEntity<Map<String, Object>> getTimeoutConfig() {
        Map<String, Object> config = new HashMap<>();
        Map<String, Long> timeoutConfig = sessionTimeoutService.getTimeoutConfig();

        config.put("success", true);
        config.put("message", "Lấy cấu hình timeout thành công");
        config.put("timeouts", timeoutConfig);

        return ResponseEntity.ok(config);
    }

    @GetMapping("/session/status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication != null && authService.getUserIdFromAuthentication(authentication) != null) {
                Long userId = authService.getUserIdFromAuthentication(authentication);

                response.put("active", true);
                response.put("userId", userId);
                response.put("message", "Session đang hoạt động");
                response.put("timeoutConfig", sessionTimeoutService.getTimeoutConfig());
            } else {
                response.put("active", false);
                response.put("message", "Session không hoạt động hoặc đã hết hạn");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("active", false);
            response.put("message", "Lỗi kiểm tra session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Kiểm tra refresh token còn hạn không
            if (!authService.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(false, "Refresh token không hợp lệ hoặc đã hết hạn"));
            }

            // Kiểm tra loại token
            String tokenType = authService.getTokenType(refreshToken);
            if (!"REFRESH".equals(tokenType)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(false, "Token không phải là refresh token"));
            }

            // Lấy thông tin user từ refresh token
            Long userId = authService.getUserIdFromToken(refreshToken);
            UserDTO user = userService.getUserByIdOrThrow(userId);

            // Tạo token mới
            // Tạo authentication giả lập để generate token
            UserPrincipal userPrincipal = UserPrincipal
                    .create(userService.findUserByUsername(user.getUsername()).orElseThrow());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(userPrincipal, null,
                    userPrincipal.getAuthorities());

            String newAccessToken = authService.generateToken(newAuth);
            String newRefreshToken = authService.generateRefreshToken(newAuth);
            long expirationTime = authService.getTokenExpirationTime(newAccessToken) / 1000;

            return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, expirationTime, user));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Không thể làm mới token: " + e.getMessage()));
        }
    }
}