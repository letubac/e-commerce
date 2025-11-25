package com.ecommerce.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Brand;
import com.ecommerce.repository.base.DbRepository;

@Repository
public interface BrandRepository extends DbRepository<Brand, Long> {

    // Maps to: brandRepository_findAll.sql
    List<Brand> findAllData();

    // Maps to: brandRepository_findById.sql
    Brand findById(@Param("id") Long id);

    // Maps to: brandRepository_existsById.sql
    boolean existsById(@Param("id") Long id);

    // Maps to: brandRepository_findByActiveTrue.sql
    List<Brand> findByActiveTrue();

    // Maps to: brandRepository_existsByNameIgnoreCase.sql
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Maps to:
    // brandRepository_findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase.sql
    Page<Brand> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);
}