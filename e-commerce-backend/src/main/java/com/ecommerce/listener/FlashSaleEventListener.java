package com.ecommerce.listener;

import com.ecommerce.dto.NotificationDTO;
import com.ecommerce.event.FlashSaleEvent;
import com.ecommerce.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Flash Sale Event Listener
 * Listens to flash sale events and broadcasts notifications to all users
 */
@Component
/**
 * author: LeTuBac
 */
public class FlashSaleEventListener {

    @Autowired
    private NotificationService notificationService;

    @EventListener
    @Async
    public void handleFlashSaleEvent(FlashSaleEvent event) {
        try {
            switch (event.getEventType()) {
                case "ACTIVATED":
                    sendFlashSaleActivatedNotification(event);
                    break;
                case "STARTING_SOON":
                    sendFlashSaleStartingSoonNotification(event);
                    break;
                case "ENDING_SOON":
                    sendFlashSaleEndingSoonNotification(event);
                    break;
                case "ENDED":
                    sendFlashSaleEndedNotification(event);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error sending Flash Sale notification: " + e.getMessage());
        }
    }

    private void sendFlashSaleActivatedNotification(FlashSaleEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setTargetRole("USER"); // Broadcast to all users
        notification.setType("FLASH_SALE");
        notification.setTitle("🔥 Flash Sale đang diễn ra!");
        notification.setMessage(String.format("%s - Giảm giá lên đến %d%%! Nhanh tay đặt hàng ngay!",
                event.getFlashSaleName(), event.getDiscountPercentage() != null ? event.getDiscountPercentage() : 0));
        notification.setLink("/flash-sale");
        notification.setIconUrl(event.getBannerUrl() != null ? event.getBannerUrl() : "/icons/flash-sale.png");
        notification.setEntityType("FlashSale");
        notification.setEntityId(event.getFlashSaleId());
        notification.setPriority("URGENT");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }

    private void sendFlashSaleStartingSoonNotification(FlashSaleEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setTargetRole("USER");
        notification.setType("FLASH_SALE");
        notification.setTitle("⏰ Flash Sale sắp bắt đầu!");
        notification.setMessage(
                String.format("%s sắp bắt đầu trong ít phút nữa! Chuẩn bị sẵn sàng nhé!", event.getFlashSaleName()));
        notification.setLink("/flash-sale");
        notification.setIconUrl(event.getBannerUrl() != null ? event.getBannerUrl() : "/icons/flash-sale-soon.png");
        notification.setEntityType("FlashSale");
        notification.setEntityId(event.getFlashSaleId());
        notification.setPriority("HIGH");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }

    private void sendFlashSaleEndingSoonNotification(FlashSaleEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setTargetRole("USER");
        notification.setType("FLASH_SALE");
        notification.setTitle("⚡ Flash Sale sắp kết thúc!");
        notification.setMessage(
                String.format("%s chỉ còn ít phút nữa là kết thúc! Đừng bỏ lỡ cơ hội!", event.getFlashSaleName()));
        notification.setLink("/flash-sale");
        notification.setIconUrl(event.getBannerUrl() != null ? event.getBannerUrl() : "/icons/flash-sale-ending.png");
        notification.setEntityType("FlashSale");
        notification.setEntityId(event.getFlashSaleId());
        notification.setPriority("URGENT");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }

    private void sendFlashSaleEndedNotification(FlashSaleEvent event) throws Exception {
        NotificationDTO notification = new NotificationDTO();
        notification.setTargetRole("USER");
        notification.setType("FLASH_SALE");
        notification.setTitle("Flash Sale đã kết thúc");
        notification.setMessage(String.format("%s đã kết thúc. Hãy theo dõi để không bỏ lỡ chương trình tiếp theo!",
                event.getFlashSaleName()));
        notification.setLink("/products");
        notification.setIconUrl("/icons/flash-sale-ended.png");
        notification.setEntityType("FlashSale");
        notification.setEntityId(event.getFlashSaleId());
        notification.setPriority("NORMAL");
        notification.setCreatedAt(new Date());

        notificationService.createNotification(notification);
    }
}
