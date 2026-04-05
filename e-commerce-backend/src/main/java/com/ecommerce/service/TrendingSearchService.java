package com.ecommerce.service;

import com.ecommerce.dto.TrendingSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * author: LeTuBac
 */
public interface TrendingSearchService {

    // Search tracking
    void recordSearch(String searchTerm);

    void recordSearch(String searchTerm, String category);

    // Trending search retrieval
    List<TrendingSearchDTO> getTrendingSearches(int limit);

    List<TrendingSearchDTO> getTrendingSearchesByCategory(String category, int limit);

    Page<TrendingSearchDTO> getAllTrendingSearches(Pageable pageable);

    // Admin management
    TrendingSearchDTO createTrendingSearch(TrendingSearchDTO trendingSearchDTO);

    TrendingSearchDTO updateTrendingSearch(TrendingSearchDTO trendingSearchDTO);

    void deleteTrendingSearch(Long id);

    TrendingSearchDTO getTrendingSearchById(Long id);

    // Search suggestions
    List<String> getSearchSuggestions(String partial, int limit);

    List<String> getSearchSuggestionsByCategory(String partial, String category, int limit);

    // Statistics
    List<TrendingSearchDTO> getTopSearchesThisWeek(int limit);

    List<TrendingSearchDTO> getTopSearchesThisMonth(int limit);

    // Admin operations
    TrendingSearchDTO activateTrendingSearch(Long id);

    TrendingSearchDTO deactivateTrendingSearch(Long id);

    void clearOldTrendingSearches(int daysOld);

    // Category management
    List<String> getAvailableCategories();

    // Bulk operations
    void resetSearchCounts();

    void recalculateTrendingSearches();
}