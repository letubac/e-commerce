package com.ecommerce.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Flash Sale Event
 * Published when Flash Sale is created, activated, or ending
 */
@Getter
public class FlashSaleEvent extends ApplicationEvent {

    private final Long flashSaleId;
    private final String flashSaleName;
    private final String eventType; // CREATED, ACTIVATED, STARTING_SOON, ENDING_SOON, ENDED
    private final Integer discountPercentage;
    private final String bannerUrl;

    public FlashSaleEvent(Object source, Long flashSaleId, String flashSaleName, String eventType,
            Integer discountPercentage, String bannerUrl) {
        super(source);
        this.flashSaleId = flashSaleId;
        this.flashSaleName = flashSaleName;
        this.eventType = eventType;
        this.discountPercentage = discountPercentage;
        this.bannerUrl = bannerUrl;
    }
}
