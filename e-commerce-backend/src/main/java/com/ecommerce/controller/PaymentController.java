package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for payment processing.
 * Provides endpoints for payment methods, processing, and management.
 */
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    /**
     * Get available payment methods
     */
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse> getPaymentMethods() {
        try {
            Map<String, Object> paymentMethods = Map.of(
                    "methods", Map.of(
                            "cod", Map.of(
                                    "id", "cod",
                                    "name", "Thanh toán khi nhận hàng (COD)",
                                    "description", "Thanh toán bằng tiền mặt khi nhận hàng",
                                    "enabled", true,
                                    "fee", 0,
                                    "icon", "cash"),
                            "bank_transfer", Map.of(
                                    "id", "bank_transfer",
                                    "name", "Chuyển khoản ngân hàng",
                                    "description", "Chuyển khoản trực tiếp qua ngân hàng",
                                    "enabled", true,
                                    "fee", 0,
                                    "icon", "bank"),
                            "momo", Map.of(
                                    "id", "momo",
                                    "name", "Ví MoMo",
                                    "description", "Thanh toán qua ví điện tử MoMo",
                                    "enabled", true,
                                    "fee", new BigDecimal("2000"),
                                    "icon", "momo"),
                            "vnpay", Map.of(
                                    "id", "vnpay",
                                    "name", "VNPay",
                                    "description", "Thanh toán qua cổng VNPay",
                                    "enabled", true,
                                    "fee", new BigDecimal("1000"),
                                    "icon", "vnpay"),
                            "zalopay", Map.of(
                                    "id", "zalopay",
                                    "name", "ZaloPay",
                                    "description", "Thanh toán qua ví ZaloPay",
                                    "enabled", true,
                                    "fee", new BigDecimal("1500"),
                                    "icon", "zalopay")));

            return ResponseEntity.ok(new ApiResponse(true, "Lấy phương thức thanh toán thành công", paymentMethods));
        } catch (Exception e) {
            log.error("Lỗi khi lấy phương thức thanh toán", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy phương thức thanh toán"));
        }
    }

    /**
     * Process payment
     */
    @PostMapping("/process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            String paymentMethod = (String) paymentRequest.get("paymentMethod");
            BigDecimal amount = new BigDecimal(paymentRequest.get("amount").toString());
            String orderId = (String) paymentRequest.get("orderId");

            Map<String, Object> paymentResult = switch (paymentMethod) {
                case "cod" -> Map.of(
                        "status", "success",
                        "message", "Đơn hàng đã được xác nhận. Bạn sẽ thanh toán khi nhận hàng.",
                        "transactionId", "COD_" + System.currentTimeMillis(),
                        "paymentMethod", "cod");
                case "bank_transfer" -> Map.of(
                        "status", "pending",
                        "message", "Vui lòng chuyển khoản theo thông tin bên dưới",
                        "transactionId", "BANK_" + System.currentTimeMillis(),
                        "paymentMethod", "bank_transfer",
                        "bankInfo", Map.of(
                                "accountNumber", "19036565556668",
                                "accountName", "CONG TY TNHH THUONG MAI DIEN TU",
                                "bankName", "Vietcombank",
                                "transferNote", "Thanh toan don hang " + orderId));
                case "momo", "vnpay", "zalopay" -> Map.of(
                        "status", "redirect",
                        "message", "Đang chuyển hướng đến cổng thanh toán",
                        "transactionId", paymentMethod.toUpperCase() + "_" + System.currentTimeMillis(),
                        "paymentMethod", paymentMethod,
                        "redirectUrl", "https://payment." + paymentMethod + ".com/gateway?order=" + orderId);
                default -> Map.of(
                        "status", "error",
                        "message", "Phương thức thanh toán không được hỗ trợ");
            };

            return ResponseEntity.ok(new ApiResponse(true, "Xử lý thanh toán thành công", paymentResult));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý thanh toán", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi xử lý thanh toán"));
        }
    }

    /**
     * Verify payment callback
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyPayment(@RequestBody Map<String, Object> verificationData) {
        try {
            String transactionId = (String) verificationData.get("transactionId");
            String status = (String) verificationData.get("status");
            String orderId = (String) verificationData.get("orderId");

            // Mock verification logic
            Map<String, Object> verificationResult = Map.of(
                    "verified", true,
                    "transactionId", transactionId,
                    "orderId", orderId,
                    "status", status,
                    "verifiedAt", System.currentTimeMillis(),
                    "message", "Thanh toán đã được xác thực thành công");

            return ResponseEntity.ok(new ApiResponse(true, "Xác thực thanh toán thành công", verificationResult));
        } catch (Exception e) {
            log.error("Lỗi khi xác thực thanh toán", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi xác thực thanh toán"));
        }
    }

    /**
     * Get payment status
     */
    @GetMapping("/status/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getPaymentStatus(@PathVariable String transactionId) {
        try {
            Map<String, Object> paymentStatus = Map.of(
                    "transactionId", transactionId,
                    "status", "completed",
                    "amount", new BigDecimal("500000"),
                    "paymentMethod", "vnpay",
                    "createdAt", System.currentTimeMillis() - 300000, // 5 minutes ago
                    "completedAt", System.currentTimeMillis() - 60000, // 1 minute ago
                    "message", "Thanh toán hoàn thành thành công");

            return ResponseEntity.ok(new ApiResponse(true, "Lấy trạng thái thanh toán thành công", paymentStatus));
        } catch (Exception e) {
            log.error("Lỗi khi lấy trạng thái thanh toán", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy trạng thái thanh toán"));
        }
    }

    /**
     * Get payment history for user
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Map<String, Object> paymentHistory = Map.of(
                    "payments", Map.of(
                            "content", java.util.List.of(
                                    Map.of(
                                            "transactionId", "VNPAY_1705123456789",
                                            "orderId", "ORD_001",
                                            "amount", new BigDecimal("750000"),
                                            "paymentMethod", "vnpay",
                                            "status", "completed",
                                            "createdAt", System.currentTimeMillis() - 86400000, // 1 day ago
                                            "completedAt", System.currentTimeMillis() - 86340000),
                                    Map.of(
                                            "transactionId", "MOMO_1705098765432",
                                            "orderId", "ORD_002",
                                            "amount", new BigDecimal("420000"),
                                            "paymentMethod", "momo",
                                            "status", "completed",
                                            "createdAt", System.currentTimeMillis() - 172800000, // 2 days ago
                                            "completedAt", System.currentTimeMillis() - 172740000),
                                    Map.of(
                                            "transactionId", "COD_1705012345678",
                                            "orderId", "ORD_003",
                                            "amount", new BigDecimal("320000"),
                                            "paymentMethod", "cod",
                                            "status", "completed",
                                            "createdAt", System.currentTimeMillis() - 259200000, // 3 days ago
                                            "completedAt", System.currentTimeMillis() - 259140000)),
                            "totalElements", 15,
                            "totalPages", 2,
                            "currentPage", page,
                            "size", size));

            return ResponseEntity.ok(new ApiResponse(true, "Lấy lịch sử thanh toán thành công", paymentHistory));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử thanh toán", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy lịch sử thanh toán"));
        }
    }

    /**
     * Get payment statistics (Admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getPaymentStatistics() {
        try {
            Map<String, Object> statistics = Map.of(
                    "totalPayments", 3420,
                    "totalAmount", new BigDecimal("125000000"),
                    "successRate", 96.5,
                    "averageAmount", new BigDecimal("365000"),
                    "paymentMethodStats", Map.of(
                            "cod", Map.of("count", 1850, "amount", new BigDecimal("67000000"), "percentage", 54.0),
                            "vnpay", Map.of("count", 890, "amount", new BigDecimal("32500000"), "percentage", 26.0),
                            "momo", Map.of("count", 420, "amount", new BigDecimal("15500000"), "percentage", 12.3),
                            "zalopay", Map.of("count", 180, "amount", new BigDecimal("6500000"), "percentage", 5.3),
                            "bank_transfer",
                            Map.of("count", 80, "amount", new BigDecimal("3500000"), "percentage", 2.4)),
                    "monthlyStats", Map.of(
                            "2024-01", Map.of("payments", 345, "amount", new BigDecimal("15000000")),
                            "2023-12", Map.of("payments", 420, "amount", new BigDecimal("18500000")),
                            "2023-11", Map.of("payments", 380, "amount", new BigDecimal("16200000"))),
                    "failureReasons", Map.of(
                            "insufficient_funds", 45,
                            "network_error", 23,
                            "expired_card", 15,
                            "user_cancelled", 38));

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thống kê thanh toán thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê thanh toán", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy thống kê thanh toán"));
        }
    }

    /**
     * Refund payment (Admin only)
     */
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> refundPayment(@RequestBody Map<String, Object> refundRequest) {
        try {
            String transactionId = (String) refundRequest.get("transactionId");
            BigDecimal refundAmount = new BigDecimal(refundRequest.get("refundAmount").toString());
            String reason = (String) refundRequest.get("reason");

            Map<String, Object> refundResult = Map.of(
                    "refundId", "REF_" + System.currentTimeMillis(),
                    "originalTransactionId", transactionId,
                    "refundAmount", refundAmount,
                    "reason", reason,
                    "status", "processing",
                    "estimatedCompletionTime", "2-5 ngày làm việc",
                    "message", "Yêu cầu hoàn tiền đã được tiếp nhận và đang xử lý");

            return ResponseEntity.ok(new ApiResponse(true, "Tạo yêu cầu hoàn tiền thành công", refundResult));
        } catch (Exception e) {
            log.error("Lỗi khi tạo yêu cầu hoàn tiền", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi tạo yêu cầu hoàn tiền"));
        }
    }

    /**
     * Get refund status
     */
    @GetMapping("/refund/{refundId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getRefundStatus(@PathVariable String refundId) {
        try {
            Map<String, Object> refundStatus = Map.of(
                    "refundId", refundId,
                    "status", "completed",
                    "refundAmount", new BigDecimal("250000"),
                    "processedAt", System.currentTimeMillis() - 86400000, // 1 day ago
                    "reason", "Sản phẩm bị lỗi",
                    "message", "Hoàn tiền đã được xử lý thành công");

            return ResponseEntity.ok(new ApiResponse(true, "Lấy trạng thái hoàn tiền thành công", refundStatus));
        } catch (Exception e) {
            log.error("Lỗi khi lấy trạng thái hoàn tiền", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy trạng thái hoàn tiền"));
        }
    }
}