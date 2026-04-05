package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Coupon;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface CouponRepository extends DbRepository<Coupon, Long> {

        // Maps to: couponRepository_findAllData.sql
        List<Coupon> findAllData();

        // Maps to: couponRepository_findById.sql
        Optional<Coupon> findById(@Param("id") Long id);

        // Maps to: couponRepository_existsById.sql
        boolean existsById(@Param("id") Long id);

        // Maps to: couponRepository_deleteById.sql
        @Modifying
        void deleteById(@Param("id") Long id);

        // Maps to: couponRepository_save.sql
        @Modifying
        Long save(
                        @Param("code") String code,
                        @Param("name") String name,
                        @Param("description") String description,
                        @Param("discountType") String discountType,
                        @Param("discountValue") Double discountValue,
                        @Param("minOrderAmount") Double minOrderAmount,
                        @Param("maxDiscountAmount") Double maxDiscountAmount,
                        @Param("usageLimit") Integer usageLimit,
                        @Param("usagePerUser") Integer usagePerUser,
                        @Param("usedCount") Integer usedCount,
                        @Param("isActive") Boolean isActive,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        @Param("createdAt") Date createdAt,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: couponRepository_update.sql
        @Modifying
        void update(
                        @Param("id") Long id,
                        @Param("code") String code,
                        @Param("name") String name,
                        @Param("description") String description,
                        @Param("discountType") String discountType,
                        @Param("discountValue") Double discountValue,
                        @Param("minOrderAmount") Double minOrderAmount,
                        @Param("maxDiscountAmount") Double maxDiscountAmount,
                        @Param("usageLimit") Integer usageLimit,
                        @Param("usageLimitPerUser") Integer usageLimitPerUser,
                        @Param("isActive") Boolean isActive,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        @Param("updatedAt") Date updatedAt);

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

        // Maps to: couponRepository_incrementUsedCount.sql
        @Modifying
        void incrementUsedCount(@Param("id") Long id);

        /**
         * Deactivate all coupons whose end_date has passed OR whose usage limit is
         * reached.
         * Maps to: CouponRepository_deactivateExpiredCoupons.sql
         */
        @Modifying
        int deactivateExpiredCoupons(@Param("now") Date now);

        Page<Coupon> findAllData(Pageable pageable);
}
