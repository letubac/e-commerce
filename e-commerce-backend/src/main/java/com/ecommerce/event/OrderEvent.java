package com.ecommerce.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Order Event
 * Published when order status changes
 */
@Getter
/**
 * author: LeTuBac
 */
public class OrderEvent extends ApplicationEvent {

    private final Long orderId;
    private final Long userId;
    private final String orderCode;
    private final String eventType; // PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private final Double totalAmount;

    public OrderEvent(Object source, Long orderId, Long userId, String orderCode, String eventType,
            Double totalAmount) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.orderCode = orderCode;
        this.eventType = eventType;
        this.totalAmount = totalAmount;
    }
}
