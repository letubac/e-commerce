package com.ecommerce.service;

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

/**
 * author: LeTuBac
 */
public interface CouponService {
    CouponDTO createCoupon(CouponDTO couponDTO) throws DetailException;

    CouponDTO updateCoupon(Long id, CouponDTO couponDTO) throws DetailException;

    void deleteCoupon(Long id) throws DetailException;

    CouponDTO getCouponById(Long id) throws DetailException;

    CouponDTO getCouponByCode(String code) throws DetailException;

    List<CouponDTO> getAllCoupons() throws DetailException;

    List<CouponDTO> getActiveCoupons() throws DetailException;

    boolean validateCoupon(String code, Double orderAmount) throws DetailException;

    Double calculateDiscount(String code, Double orderAmount) throws DetailException;

    void useCoupon(String code) throws DetailException;

    // Additional methods for controller compatibility
    Page<CouponDTO> findAll(Pageable pageable) throws DetailException;

    CouponDTO findById(Long id) throws DetailException;

    CouponDTO findByCode(String code) throws DetailException;

    CouponDTO save(CouponDTO couponDTO) throws DetailException;

    CouponDTO update(CouponDTO couponDTO) throws DetailException;

    void deleteById(Long id) throws DetailException;

    CouponDTO validateCoupon(String code) throws DetailException;

    CouponDTO toggleActiveStatus(Long id) throws DetailException;

    Map<String, Object> getCouponStatistics(Long id) throws DetailException;

    Map<String, Object> applyCoupon(String code, Double orderAmount) throws DetailException;

    List<CouponDTO> findActiveCoupons() throws DetailException;

    Page<CouponDTO> searchCoupons(String keyword, Pageable pageable) throws DetailException;
}
