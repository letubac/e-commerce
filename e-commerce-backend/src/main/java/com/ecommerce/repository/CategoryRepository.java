package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Category;
import com.ecommerce.repository.base.DbRepository;

@Repository
public interface CategoryRepository extends DbRepository<Category, Long> {

    List<Category> findAllData();

    Optional<Category> findById(@Param("id") Long id);

    boolean existsById(@Param("id") Long id);

    List<Category> findByActiveTrue();

    boolean existsByNameIgnoreCase(@Param("name") String name);

    Page<Category> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("search") String search, Pageable pageable);
}