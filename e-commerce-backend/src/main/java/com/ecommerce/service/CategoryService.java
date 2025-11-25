package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing categories
 */
public interface CategoryService {

    /**
     * Get all active categories
     */
    List<CategoryDTO> findActiveCategories();

    /**
     * Get all categories (including inactive) - Admin only
     */
    List<CategoryDTO> findAll();

    /**
     * Get category by ID
     */
    CategoryDTO findById(Long id);

    /**
     * Create new category
     */
    CategoryDTO save(CategoryDTO categoryDTO);

    /**
     * Update category
     */
    CategoryDTO update(CategoryDTO categoryDTO);

    /**
     * Delete category by ID
     */
    void deleteById(Long id);

    /**
     * Check if category exists
     */
    boolean existsById(Long id);

    /**
     * Toggle category active status
     */
    CategoryDTO toggleActiveStatus(Long id);

    /**
     * Search categories
     */
    Page<CategoryDTO> search(String keyword, Pageable pageable);

    /**
     * Get categories with pagination
     */
    Page<CategoryDTO> findAll(Pageable pageable);
}