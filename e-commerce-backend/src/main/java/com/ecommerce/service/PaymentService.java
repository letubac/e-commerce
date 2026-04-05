package com.ecommerce.service;

import com.ecommerce.exception.DetailException;

import java.util.Map;

/**
 * Service interface for payment operations
 */
/**
 * author: LeTuBac
 */
public interface PaymentService {

    /**
     * Get available payment methods
     */
    Map<String, Object> getPaymentMethods() throws DetailException;

    /**
     * Process payment
     */
    Map<String, Object> processPayment(Map<String, Object> paymentRequest) throws DetailException;

    /**
     * Verify payment callback
     */
    Map<String, Object> verifyPayment(Map<String, Object> verificationData) throws DetailException;

    /**
     * Get payment status by transaction ID
     */
    Map<String, Object> getPaymentStatus(String transactionId) throws DetailException;

    /**
     * Get payment history for user
     */
    Map<String, Object> getPaymentHistory(int page, int size) throws DetailException;

    /**
     * Get payment statistics (Admin)
     */
    Map<String, Object> getPaymentStatistics() throws DetailException;

    /**
     * Create refund request (Admin)
     */
    Map<String, Object> createRefund(Map<String, Object> refundRequest) throws DetailException;

    /**
     * Get refund status
     */
    Map<String, Object> getRefundStatus(String refundId) throws DetailException;
}
