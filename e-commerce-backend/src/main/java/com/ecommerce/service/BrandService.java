package com.ecommerce.service;

import com.ecommerce.dto.BrandDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing brands
 */
public interface BrandService {

    /**
     * Get all active brands
     */
    List<BrandDTO> findActiveBrands();

    /**
     * Get all brands (including inactive) - Admin only
     */
    List<BrandDTO> findAll();

    /**
     * Get brand by ID
     */
    BrandDTO findById(Long id);

    /**
     * Create new brand
     */
    BrandDTO save(BrandDTO brandDTO);

    /**
     * Update brand
     */
    BrandDTO update(BrandDTO brandDTO);

    /**
     * Delete brand by ID
     */
    void deleteById(Long id);

    /**
     * Check if brand exists
     */
    boolean existsById(Long id);

    /**
     * Toggle brand active status
     */
    BrandDTO toggleActiveStatus(Long id);

    /**
     * Search brands
     */
    Page<BrandDTO> search(String keyword, Pageable pageable);

    /**
     * Get brands with pagination
     */
    Page<BrandDTO> findAll(Pageable pageable);

    /**
     * Get brand statistics
     */
    Map<String, Object> getBrandStatistics(Long brandId);

    /**
     * Get popular brands
     */
    List<BrandDTO> getPopularBrands(int limit);
}