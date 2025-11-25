package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Coupon;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
public interface CouponRepository extends DbRepository<Coupon, Long> {

    // Maps to: couponRepository_findAll.sql
    List<Coupon> findAllData();

    // Maps to: couponRepository_findById.sql
    Optional<Coupon> findById(@Param("id") Long id);

    // Maps to: couponRepository_existsById.sql
    boolean existsById(@Param("id") Long id);

    // Maps to: couponRepository_deleteById.sql
    @Modifying
    @Transactional
    void deleteById(@Param("id") Long id);

    // Maps to: couponRepository_findByCode.sql
    Optional<Coupon> findByCode(@Param("code") String code);

    // Maps to: couponRepository_existsByCode.sql
    boolean existsByCode(@Param("code") String code);

    // Maps to: couponRepository_findByIsActive.sql
    List<Coupon> findByIsActive(@Param("isActive") Boolean isActive);

    // Maps to: couponRepository_findValidCoupons.sql
    List<Coupon> findValidCoupons(@Param("now") Date now);

    // Maps to: couponRepository_findValidCouponByCode.sql
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("now") Date now);

    // Maps to: couponRepository_findExpiredCoupons.sql
    List<Coupon> findExpiredCoupons(@Param("now") Date now);

    // Maps to: couponRepository_findFullyUsedCoupons.sql
    List<Coupon> findFullyUsedCoupons();
}
