package com.ecommerce.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Brand;
import com.ecommerce.repository.base.DbRepository;
import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface BrandRepository extends DbRepository<Brand, Long> {

    // Maps to: BrandRepository_findAllData.sql
    List<Brand> findAllData();

    // Maps to: BrandRepository_findById.sql
    Brand findById(@Param("id") Long id);

    // Maps to: BrandRepository_existsById.sql
    boolean existsById(@Param("id") Long id);

    // Maps to: BrandRepository_findByActiveTrue.sql
    List<Brand> findByActiveTrue();

    // Maps to: BrandRepository_existsByNameIgnoreCase.sql
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Maps to: BrandRepository_findByExactName.sql
    Brand findByExactName(@Param("name") String name);

    // Maps to:
    // BrandRepository_findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase.sql
    Page<Brand> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);

    Long countAllByActiveTrue();

    // Custom write methods — map to custom SQL files instead of MirageSQL built-ins
    @Modifying
    Integer insertBrand(@Param("name") String name, @Param("slug") String slug,
            @Param("description") String description, @Param("logoUrl") String logoUrl,
            @Param("websiteUrl") String websiteUrl, @Param("isActive") boolean isActive,
            @Param("createdAt") Date createdAt, @Param("updatedAt") Date updatedAt);

    @Modifying
    Integer updateBrand(@Param("id") Long id, @Param("name") String name, @Param("slug") String slug,
            @Param("description") String description, @Param("logoUrl") String logoUrl,
            @Param("websiteUrl") String websiteUrl, @Param("isActive") boolean isActive,
            @Param("updatedAt") Date updatedAt);

    @Modifying
    Integer toggleActiveStatus(@Param("id") Long id, @Param("isActive") boolean isActive,
            @Param("updatedAt") Date updatedAt);

    @Modifying
    Integer deleteBrand(@Param("id") Long id);
}