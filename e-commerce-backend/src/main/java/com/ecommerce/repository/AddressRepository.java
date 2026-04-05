package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Address;
import com.ecommerce.repository.base.DbRepository;

@Repository
/**
 * author: LeTuBac
 */
public interface AddressRepository extends DbRepository<Address, Long> {

    // Maps to: addressRepository_findById.sql
    Optional<Address> findById(@Param("id") Long id);

    // Maps to: addressRepository_findByUserId.sql
    List<Address> findByUserId(@Param("userId") Long userId);

    // Maps to: addressRepository_findDefaultByUserId.sql
//    Optional<Address> findDefaultByUserId(@Param("userId") Long userId);
}
