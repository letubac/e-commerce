package com.ecommerce.listener;

import com.ecommerce.dto.NotificationDTO;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Order Event Listener
 * Listens to order events and sends notifications
 */
@Component
public class OrderEventListener {

    @Autowired
    private NotificationService notificationService;

    @EventListener
    @Async
    public void handleOrderEvent(OrderEvent event) {
        try {
            switch (event.getEventType()) {
                case "PLACED":
                    sendOrderPlacedNotifications(event);
                    break;
                case "CONFIRMED":
                    sendOrderConfirmedNotification(event);
                    break;
                case "SHIPPED":
                    sendOrderShippedNotification(event);
                    break;
                case "DELIVERED":
                    sendOrderDeliveredNotification(event);
                    break;
                case "CANCELLED":
                    sendOrderCancelledNotification(event);
                    break;
            }
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting main flow
            System.err.println("Error sending notification for order event: " + e.getMessage());
        }
    }

    private void sendOrderPlacedNotifications(OrderEvent event) throws Exception {
        // Notify customer
        NotificationDTO customerNotification = new NotificationDTO();
        customerNotification.setUserId(event.getUserId());
        customerNotification.setType("ORDER");
        customerNotification.setTitle("Đơn hàng đã được đặt thành công");
        customerNotification.setMessage(String.format("Đơn hàng #%s của bạn đã được đặt thành công. Tổng tiền: %,.0fđ",
                event.getOrderCode(), event.getTotalAmount()));
        customerNotification.setLink("/orders/" + event.getOrderId());
        customerNotification.setIconUrl("/icons/order-success.png");
        customerNotification.setEntityType("Order");
        customerNotification.setEntityId(event.getOrderId());
        customerNotification.setPriority("NORMAL");
        customerNotification.setCreatedAt(new Date());

        notificationService.createNotification(customerNotification);

        // Notify admins
        NotificationDTO adminNotification = new NotificationDTO();
        adminNotification.setTargetRole("ADMIN");
        adminNotification.setType("ORDER");
        adminNotification.setTitle("Đơn hàng mới");
        adminNotification.setMessage(String.format("Đơn hàng mới #%s - Tổng tiền: %,.0fđ",
                event.getOrderCode(), event.getTotalAmount()));
        adminNotification.setLink("/admin/orders/" + event.getOrderId());
        adminNotification.setIconUrl("/icons/new-order.png");
        adminNotification.setEntityType("Order");
        adminNotification.setEntityId(event.getOrderId());
        adminNotification.setPriority("HIGH");
        adminNotification.setCreatedAt(new Date());

        notificationService.createNotification(adminNotification);
    }

    private void sendOrderConfirmedNotification(OrderEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(event.getUserId());
        notification.setType("ORDER");
        notification.setTitle("Đơn hàng đã được xác nhận");
        notification
                .setMessage(String.format("Đơn hàng #%s đã được xác nhận và đang được chuẩn bị", event.getOrderCode()));
        notification.setLink("/orders/" + event.getOrderId());
        notification.setIconUrl("/icons/order-confirmed.png");
        notification.setEntityType("Order");
        notification.setEntityId(event.getOrderId());
        notification.setPriority("NORMAL");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }

    private void sendOrderShippedNotification(OrderEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(event.getUserId());
        notification.setType("ORDER");
        notification.setTitle("Đơn hàng đang được giao");
        notification.setMessage(String.format("Đơn hàng #%s đã được giao cho đơn vị vận chuyển", event.getOrderCode()));
        notification.setLink("/orders/" + event.getOrderId());
        notification.setIconUrl("/icons/order-shipped.png");
        notification.setEntityType("Order");
        notification.setEntityId(event.getOrderId());
        notification.setPriority("HIGH");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }

    private void sendOrderDeliveredNotification(OrderEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(event.getUserId());
        notification.setType("ORDER");
        notification.setTitle("Đơn hàng đã được giao thành công");
        notification.setMessage(
                String.format("Đơn hàng #%s đã được giao đến bạn. Cảm ơn bạn đã mua hàng!", event.getOrderCode()));
        notification.setLink("/orders/" + event.getOrderId());
        notification.setIconUrl("/icons/order-delivered.png");
        notification.setEntityType("Order");
        notification.setEntityId(event.getOrderId());
        notification.setPriority("NORMAL");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }

    private void sendOrderCancelledNotification(OrderEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(event.getUserId());
        notification.setType("ORDER");
        notification.setTitle("Đơn hàng đã bị hủy");
        notification.setMessage(
                String.format("Đơn hàng #%s đã bị hủy. Nếu bạn đã thanh toán, số tiền sẽ được hoàn lại trong 5-7 ngày.",
                        event.getOrderCode()));
        notification.setLink("/orders/" + event.getOrderId());
        notification.setIconUrl("/icons/order-cancelled.png");
        notification.setEntityType("Order");
        notification.setEntityId(event.getOrderId());
        notification.setPriority("HIGH");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }
}
