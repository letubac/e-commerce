package com.ecommerce.repository.base;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;

import vn.com.unit.sparwings.spring.data.repository.ChunkableRepository;
import vn.com.unit.sparwings.spring.data.repository.PageableRepository;
import vn.com.unit.sparwings.spring.data.repository.ScannableRepository;
import vn.com.unit.sparwings.spring.data.repository.UpsertableRepository;
import vn.com.unit.sparwings.spring.data.repository.WritableRepository;

/**
 * Base repository interface for custom SQL file-based repositories
 * Each method automatically maps to SQL file: {repositoryName}_{methodName}.sql
 * 
 * Example:
 * - UserRepository.findById(@Param("id") Long id)
 * maps to: userRepository_findById.sql
 * - UserRepository.findByUsername(@Param("username") String username)
 * maps to: userRepository_findByUsername.sql
 */
@NoRepositoryBean
/**
 * author: LeTuBac
 */
public interface DbRepository<E, ID extends Serializable>
        extends ScannableRepository<E, ID>, UpsertableRepository<E, ID>,
        WritableRepository<E, ID>, ChunkableRepository<E, ID>, PageableRepository<E, ID> {

}