package com.ecommerce.service;

import com.ecommerce.dto.CouponDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface CouponService {
    CouponDTO createCoupon(CouponDTO couponDTO);

    CouponDTO updateCoupon(Long id, CouponDTO couponDTO);

    void deleteCoupon(Long id);

    CouponDTO getCouponById(Long id);

    CouponDTO getCouponByCode(String code);

    List<CouponDTO> getAllCoupons();

    List<CouponDTO> getActiveCoupons();

    boolean validateCoupon(String code, Double orderAmount);

    Double calculateDiscount(String code, Double orderAmount);

    void useCoupon(String code);

    // Additional methods for controller compatibility
    Page<CouponDTO> findAll(Pageable pageable);

    CouponDTO findById(Long id);

    CouponDTO findByCode(String code);

    CouponDTO save(CouponDTO couponDTO);

    CouponDTO update(CouponDTO couponDTO);

    void deleteById(Long id);

    CouponDTO validateCoupon(String code);

    CouponDTO toggleActiveStatus(Long id);

    Map<String, Object> getCouponStatistics(Long id);

    Map<String, Object> applyCoupon(String code, Double orderAmount);

    List<CouponDTO> findActiveCoupons();

    Page<CouponDTO> searchCoupons(String keyword, Pageable pageable);
}