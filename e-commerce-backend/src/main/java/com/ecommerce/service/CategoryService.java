package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing categories
 */
/**
 * author: LeTuBac
 */
public interface CategoryService {

    /**
     * Get all active categories
     */
    List<CategoryDTO> findActiveCategories() throws DetailException;

    /**
     * Get all categories (including inactive) - Admin only
     */
    List<CategoryDTO> findAll() throws DetailException;

    /**
     * Get category by ID
     */
    CategoryDTO findById(Long id) throws DetailException;

    /**
     * Create new category
     */
    CategoryDTO save(CategoryDTO categoryDTO) throws DetailException;

    /**
     * Update category
     */
    CategoryDTO update(CategoryDTO categoryDTO) throws DetailException;

    /**
     * Delete category by ID
     */
    void deleteById(Long id) throws DetailException;

    /**
     * Check if category exists
     */
    boolean existsById(Long id) throws DetailException;

    /**
     * Toggle category active status
     */
    CategoryDTO toggleActiveStatus(Long id) throws DetailException;

    /**
     * Search categories
     */
    Page<CategoryDTO> search(String keyword, Pageable pageable) throws DetailException;

    /**
     * Get categories with pagination
     */
    Page<CategoryDTO> findAll(Pageable pageable) throws DetailException;
}