package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class TrendingSearchDTO {
    private Long id;
    private String searchTerm;
    private Integer searchCount;
    private String category;
    private boolean isActive;
    private Date lastSearched;
    private Date createdAt;

    // Additional fields for display
    private boolean popular;
    private String displayText;
    private int rankPosition;
    private double growthRate;
}
