package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Category;
import com.ecommerce.repository.base.DbRepository;
import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface CategoryRepository extends DbRepository<Category, Long> {

    List<Category> findAllData();

    Optional<Category> findById(@Param("id") Long id);

    boolean existsById(@Param("id") Long id);

    List<Category> findByActiveTrue();

    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Maps to: CategoryRepository_findByExactName.sql
    Optional<Category> findByExactName(@Param("name") String name);

    Page<Category> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);

    Long countAllByActiveTrue();

    // Custom write methods — map to custom SQL files instead of MirageSQL built-ins
    @Modifying
    Integer insertCategory(@Param("name") String name, @Param("slug") String slug,
            @Param("description") String description, @Param("parentId") Long parentId,
            @Param("imageUrl") String imageUrl, @Param("sortOrder") Integer sortOrder,
            @Param("isActive") boolean isActive,
            @Param("createdAt") Date createdAt, @Param("updatedAt") Date updatedAt);

    @Modifying
    Integer updateCategory(@Param("id") Long id, @Param("name") String name, @Param("slug") String slug,
            @Param("description") String description, @Param("parentId") Long parentId,
            @Param("imageUrl") String imageUrl, @Param("sortOrder") Integer sortOrder,
            @Param("isActive") boolean isActive, @Param("updatedAt") Date updatedAt);

    @Modifying
    Integer deleteCategory(@Param("id") Long id);
}