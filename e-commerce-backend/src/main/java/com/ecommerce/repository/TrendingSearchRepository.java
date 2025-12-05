package com.ecommerce.repository;

import com.ecommerce.entity.TrendingSearch;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingSearchRepository extends DbRepository<TrendingSearch, Long> {

    // Maps to: trendingSearchRepository_findActive.sql
    List<TrendingSearch> findActive();

    // Maps to: trendingSearchRepository_findByCategory.sql
    List<TrendingSearch> findByCategory(@Param("category") String category);

    // Maps to: trendingSearchRepository_findTop.sql
    List<TrendingSearch> findTop(@Param("limit") int limit);

    // Maps to: trendingSearchRepository_incrementSearchCount.sql
    @Modifying
    int incrementSearchCount(@Param("keyword") String keyword);
}