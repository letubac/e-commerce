package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class FlashSaleDTO {
    private Long id;
    private String name;
    private String description;
    private Date startTime;
    private Date endTime;
    @JsonProperty("isActive")
    private boolean isActive;
    private String bannerImageUrl;
    private String backgroundColor;
    private Date createdAt;
    private Date updatedAt;

    // Additional fields for display
    private List<FlashSaleProductDTO> products;
    private boolean currentlyActive;
    private boolean upcoming;
    private boolean expired;
    private long remainingTimeInMinutes;
    private int totalProducts;
    @JsonProperty("hasProducts")
    private boolean hasProducts;
    private Long totalSales;
    private java.math.BigDecimal totalRevenue;
}
