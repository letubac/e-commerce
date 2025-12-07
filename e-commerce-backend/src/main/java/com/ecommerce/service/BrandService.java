package com.ecommerce.service;

import com.ecommerce.dto.BrandDTO;
import com.ecommerce.exception.DetailException;

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
    List<BrandDTO> findActiveBrands() throws DetailException;

    /**
     * Get all brands (including inactive) - Admin only
     */
    List<BrandDTO> findAll() throws DetailException;

    /**
     * Get brand by ID
     */
    BrandDTO findById(Long id) throws DetailException;

    /**
     * Create new brand
     */
    BrandDTO save(BrandDTO brandDTO) throws DetailException;

    /**
     * Update brand
     */
    BrandDTO update(BrandDTO brandDTO) throws DetailException;

    /**
     * Delete brand by ID
     */
    void deleteById(Long id) throws DetailException;

    /**
     * Check if brand exists
     */
    boolean existsById(Long id) throws DetailException;

    /**
     * Toggle brand active status
     */
    BrandDTO toggleActiveStatus(Long id) throws DetailException;

    /**
     * Search brands
     */
    Page<BrandDTO> search(String keyword, Pageable pageable) throws DetailException;

    /**
     * Get brands with pagination
     */
    Page<BrandDTO> findAll(Pageable pageable) throws DetailException;

    /**
     * Get brand statistics
     */
    Map<String, Object> getBrandStatistics(Long brandId) throws DetailException;

    /**
     * Get popular brands
     */
    List<BrandDTO> getPopularBrands(int limit) throws DetailException;
}