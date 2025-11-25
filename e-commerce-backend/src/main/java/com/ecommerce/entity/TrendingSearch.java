package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trending_searches")
public class TrendingSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
