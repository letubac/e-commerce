package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.TRENDING_SEARCHES)
/**
 * author: LeTuBac
 */
public class TrendingSearch {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.TRENDING_SEARCHES)
    @Column(name = "id")
    private Long id;
    @Column(name = "search_term")
    private String searchTerm;

    @Column(name = "search_count")
    private Integer searchCount;

    @Column(name = "category")
    private String category;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "last_searched")
    private Date lastSearched;

    @Column(name = "created_at")
    private Date createdAt;

    // Business methods
    public void incrementSearchCount() {
        this.searchCount = (this.searchCount != null ? this.searchCount : 0) + 1;
        this.lastSearched = new Date();
    }

    public boolean isPopular() {
        return searchCount != null && searchCount >= 100;
    }

    public String getDisplayText() {
        return searchTerm + (isPopular() ? " 🔥" : "");
    }
}
