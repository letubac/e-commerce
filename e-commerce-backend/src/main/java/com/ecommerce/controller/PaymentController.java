package com.ecommerce.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.PaymentService;
import com.ecommerce.webapp.BusinessApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for payment processing.
 * Provides endpoints for payment methods, processing, and management.
 */
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
/**
 * author: LeTuBac
 */
public class PaymentController {

        private final PaymentService paymentService;
        private final ErrorHandler errorHandler;
        private final SuccessHandler successHandler;

        /**
         * Get available payment methods
         */
        @GetMapping("/methods")
        public ResponseEntity<BusinessApiResponse> getPaymentMethods() {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Lấy danh sách phương thức thanh toán");
                        Map<String, Object> paymentMethods = paymentService.getPaymentMethods();
                        return ResponseEntity.ok(successHandler.handlerSuccess(paymentMethods, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Process payment
         */
        @PostMapping("/process")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<BusinessApiResponse> processPayment(@RequestBody Map<String, Object> paymentRequest) {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Xử lý thanh toán: {}", paymentRequest);
                        Map<String, Object> paymentResult = paymentService.processPayment(paymentRequest);
                        return ResponseEntity.ok(successHandler.handlerSuccess(paymentResult, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Verify payment callback
         */
        @PostMapping("/verify")
        public ResponseEntity<BusinessApiResponse> verifyPayment(@RequestBody Map<String, Object> verificationData) {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Xác thực thanh toán: {}", verificationData);
                        Map<String, Object> verificationResult = paymentService.verifyPayment(verificationData);
                        return ResponseEntity.ok(successHandler.handlerSuccess(verificationResult, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Get payment status
         */
        @GetMapping("/status/{transactionId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<BusinessApiResponse> getPaymentStatus(
                        @PathVariable(name = "transactionId") String transactionId) {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Lấy trạng thái thanh toán: {}", transactionId);
                        Map<String, Object> paymentStatus = paymentService.getPaymentStatus(transactionId);
                        return ResponseEntity.ok(successHandler.handlerSuccess(paymentStatus, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Get payment history for user
         */
        @GetMapping("/history")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<BusinessApiResponse> getPaymentHistory(
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size) {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Lấy lịch sử thanh toán - page: {}, size: {}", page, size);
                        Map<String, Object> paymentHistory = paymentService.getPaymentHistory(page, size);
                        return ResponseEntity.ok(successHandler.handlerSuccess(paymentHistory, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Get payment statistics (Admin only)
         */
        @GetMapping("/statistics")
        // @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusinessApiResponse> getPaymentStatistics() {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Lấy thống kê thanh toán");
                        Map<String, Object> statistics = paymentService.getPaymentStatistics();
                        return ResponseEntity.ok(successHandler.handlerSuccess(statistics, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Refund payment (Admin only)
         */
        @PostMapping("/refund")
        // @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusinessApiResponse> refundPayment(@RequestBody Map<String, Object> refundRequest) {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Tạo yêu cầu hoàn tiền: {}", refundRequest);
                        Map<String, Object> refundResult = paymentService.createRefund(refundRequest);
                        return ResponseEntity.ok(successHandler.handlerSuccess(refundResult, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }

        /**
         * Get refund status
         */
        @GetMapping("/refund/{refundId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<BusinessApiResponse> getRefundStatus(@PathVariable(name = "refundId") String refundId) {
                long start = System.currentTimeMillis();
                try {
                        log.debug("API: Lấy trạng thái hoàn tiền: {}", refundId);
                        Map<String, Object> refundStatus = paymentService.getRefundStatus(refundId);
                        return ResponseEntity.ok(successHandler.handlerSuccess(refundStatus, start));
                } catch (Exception e) {
                        return ResponseEntity.ok(errorHandler.handlerException(e, start));
                }
        }
}