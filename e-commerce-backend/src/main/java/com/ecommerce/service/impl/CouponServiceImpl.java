package com.ecommerce.service.impl;

import com.ecommerce.constant.CouponConstant;
import com.ecommerce.dto.CouponDTO;
import com.ecommerce.entity.Coupon;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Implementation of CouponService with real database operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CouponServiceImpl implements CouponService {

	private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);

	private final CouponRepository couponRepository;

	@Override
	public CouponDTO createCoupon(CouponDTO couponDTO) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Tạo coupon mới với code: {}", couponDTO.getCode());

			// Validation
			if (couponDTO.getCode() == null || couponDTO.getCode().trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			if (couponDTO.getDiscountValue() == null || couponDTO.getDiscountValue().doubleValue() <= 0) {
				throw new DetailException(CouponConstant.E612_INVALID_DISCOUNT_VALUE);
			}

			// Validate coupon code uniqueness
			if (couponRepository.existsByCode(couponDTO.getCode())) {
				throw new DetailException(CouponConstant.E608_COUPON_CODE_EXISTS);
			}

			// Validate dates
			if (couponDTO.getStartDate() != null && couponDTO.getEndDate() != null) {
				if (couponDTO.getStartDate().after(couponDTO.getEndDate())) {
					throw new DetailException(CouponConstant.E616_INVALID_DATE_RANGE);
				}
			}

			Date now = new Date();

			// Save to database using repository method
			Long couponId = couponRepository.save(
					couponDTO.getCode(),
					couponDTO.getName(),
					couponDTO.getDescription(),
					couponDTO.getDiscountType(),
					couponDTO.getDiscountValue() != null ? couponDTO.getDiscountValue().doubleValue() : 0.0,
					couponDTO.getMinOrderAmount() != null ? couponDTO.getMinOrderAmount().doubleValue() : 0.0,
					couponDTO.getMaxDiscountAmount() != null ? couponDTO.getMaxDiscountAmount().doubleValue() : 0.0,
					couponDTO.getUsageLimit() != null ? couponDTO.getUsageLimit() : 0,
					couponDTO.getUsageLimitPerUser() != null ? couponDTO.getUsageLimitPerUser() : 0,
					0, // usedCount starts at 0
					couponDTO.getIsActive() != null ? couponDTO.getIsActive() : true,
					couponDTO.getStartDate(),
					couponDTO.getEndDate(),
					now,
					now);

			log.info("Đã tạo coupon thành công với ID: {}, Code: {} - took: {}ms", couponId,
					couponDTO.getCode(), System.currentTimeMillis() - start);

			// Retrieve and return the saved coupon
			Coupon savedCoupon = couponRepository.findById(couponId)
					.orElseThrow(() -> new DetailException(CouponConstant.E605_COUPON_CREATION_FAILED));

			return convertEntityToDto(savedCoupon);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi tạo coupon", e);
			throw new DetailException(CouponConstant.E605_COUPON_CREATION_FAILED);
		}
	}

	@Override
	public CouponDTO updateCoupon(Long id, CouponDTO couponDTO) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Cập nhật coupon ID: {}", id);

			// Validation
			if (id == null || id <= 0) {
				throw new DetailException(CouponConstant.E610_INVALID_COUPON_ID);
			}

			if (couponDTO.getCode() == null || couponDTO.getCode().trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			Coupon existingCoupon = couponRepository.findById(id)
					.orElseThrow(() -> new DetailException(CouponConstant.E600_COUPON_NOT_FOUND));

			// Check if code is being changed and if new code already exists
			if (!existingCoupon.getCode().equals(couponDTO.getCode())
					&& couponRepository.existsByCode(couponDTO.getCode())) {
				throw new DetailException(CouponConstant.E608_COUPON_CODE_EXISTS);
			}

			// Validate dates
			if (couponDTO.getStartDate() != null && couponDTO.getEndDate() != null) {
				if (couponDTO.getStartDate().after(couponDTO.getEndDate())) {
					throw new DetailException(CouponConstant.E616_INVALID_DATE_RANGE);
				}
			}

			// Update using repository method
			couponRepository.update(
					id,
					couponDTO.getCode(),
					couponDTO.getName(),
					couponDTO.getDescription(),
					couponDTO.getDiscountType(),
					couponDTO.getDiscountValue() != null ? couponDTO.getDiscountValue().doubleValue() : 0.0,
					couponDTO.getMinOrderAmount() != null ? couponDTO.getMinOrderAmount().doubleValue() : 0.0,
					couponDTO.getMaxDiscountAmount() != null ? couponDTO.getMaxDiscountAmount().doubleValue() : 0.0,
					couponDTO.getUsageLimit(),
					couponDTO.getUsageLimitPerUser(),
					couponDTO.getIsActive(),
					couponDTO.getStartDate(),
					couponDTO.getEndDate(),
					new Date());

			log.info("Đã cập nhật coupon thành công với ID: {} - took: {}ms", id,
					System.currentTimeMillis() - start);

			// Retrieve and return updated coupon
			Coupon updatedCoupon = couponRepository.findById(id)
					.orElseThrow(() -> new DetailException(CouponConstant.E606_COUPON_UPDATE_FAILED));

			return convertEntityToDto(updatedCoupon);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi cập nhật coupon ID: {}", id, e);
			throw new DetailException(CouponConstant.E606_COUPON_UPDATE_FAILED);
		}
	}

	@Override
	public void deleteCoupon(Long id) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Xóa coupon ID: {}", id);

			if (id == null || id <= 0) {
				throw new DetailException(CouponConstant.E610_INVALID_COUPON_ID);
			}

			if (!couponRepository.existsById(id)) {
				throw new DetailException(CouponConstant.E600_COUPON_NOT_FOUND);
			}

			couponRepository.deleteById(id);
			log.info("Đã xóa coupon thành công với ID: {} - took: {}ms", id,
					System.currentTimeMillis() - start);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi xóa coupon ID: {}", id, e);
			throw new DetailException(CouponConstant.E607_COUPON_DELETE_FAILED);
		}
	}

	@Override
	public CouponDTO getCouponById(Long id) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy coupon theo ID: {}", id);

			if (id == null || id <= 0) {
				throw new DetailException(CouponConstant.E610_INVALID_COUPON_ID);
			}

			Coupon coupon = couponRepository.findById(id)
					.orElseThrow(() -> new DetailException(CouponConstant.E600_COUPON_NOT_FOUND));

			log.info("Lấy coupon ID: {} thành công - took: {}ms", id, System.currentTimeMillis() - start);
			return convertEntityToDto(coupon);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy coupon ID: {}", id, e);
			throw new DetailException(CouponConstant.E602_COUPON_FETCH_FAILED);
		}
	}

	@Override
	public CouponDTO getCouponByCode(String code) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy coupon theo code: {}", code);

			if (code == null || code.trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			Coupon coupon = couponRepository.findByCode(code.trim())
					.orElseThrow(() -> new DetailException(CouponConstant.E601_COUPON_CODE_NOT_FOUND));

			log.info("Lấy coupon code: {} thành công - took: {}ms", code, System.currentTimeMillis() - start);
			return convertEntityToDto(coupon);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy coupon code: {}", code, e);
			throw new DetailException(CouponConstant.E602_COUPON_FETCH_FAILED);
		}
	}

	@Override
	public List<CouponDTO> getAllCoupons() throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy tất cả coupon");

			List<Coupon> coupons = couponRepository.findAllData();
			log.info("Lấy {} coupons thành công - took: {}ms", coupons.size(),
					System.currentTimeMillis() - start);
			return coupons.stream().map(this::convertEntityToDto).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Lỗi khi lấy tất cả coupon", e);
			throw new DetailException(CouponConstant.E602_COUPON_FETCH_FAILED);
		}
	}

	@Override
	public List<CouponDTO> getActiveCoupons() throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy danh sách coupon đang hoạt động");

			List<Coupon> coupons = couponRepository.findValidCoupons(new Date());
			log.info("Lấy {} active coupons thành công - took: {}ms", coupons.size(),
					System.currentTimeMillis() - start);
			return coupons.stream().map(this::convertEntityToDto).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Lỗi khi lấy active coupons", e);
			throw new DetailException(CouponConstant.E602_COUPON_FETCH_FAILED);
		}
	}

	@Override
	public boolean validateCoupon(String code, Double orderAmount) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Validate coupon: {} cho đơn hàng: {}", code, orderAmount);

			if (code == null || code.trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			if (orderAmount == null || orderAmount <= 0) {
				throw new DetailException(CouponConstant.E618_INVALID_ORDER_AMOUNT);
			}

			Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code.trim(), new Date());
			if (couponOpt.isEmpty()) {
				log.debug("Coupon {} không hợp lệ hoặc không tồn tại - took: {}ms", code,
						System.currentTimeMillis() - start);
				return false;
			}

			Coupon coupon = couponOpt.get();
			boolean isValid = coupon.isValid()
					&& (coupon.getMinOrderAmount() == null || orderAmount >= coupon.getMinOrderAmount().doubleValue());

			log.info("Validate coupon {} kết quả: {} - took: {}ms", code, isValid,
					System.currentTimeMillis() - start);
			return isValid;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi validate coupon: {}", code, e);
			throw new DetailException(CouponConstant.E626_COUPON_VALIDATION_FAILED);
		}
	}

	@Override
	public Double calculateDiscount(String code, Double orderAmount) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Tính discount cho coupon: {} với order amount: {}", code, orderAmount);

			if (code == null || code.trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			if (orderAmount == null || orderAmount <= 0) {
				throw new DetailException(CouponConstant.E618_INVALID_ORDER_AMOUNT);
			}

			Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code.trim(), new Date());
			if (couponOpt.isEmpty()) {
				throw new DetailException(CouponConstant.E601_COUPON_CODE_NOT_FOUND);
			}

			Coupon coupon = couponOpt.get();
			Double discount = coupon.calculateDiscount(BigDecimal.valueOf(orderAmount)).doubleValue();

			log.info("Tính discount coupon {} thành công: {} - took: {}ms", code, discount,
					System.currentTimeMillis() - start);
			return discount;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi tính discount cho coupon: {}", code, e);
			throw new DetailException(CouponConstant.E602_COUPON_FETCH_FAILED);
		}
	}

	@Override
	public void useCoupon(String code) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Sử dụng coupon: {}", code);

			if (code == null || code.trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			Coupon coupon = couponRepository.findByCode(code.trim())
					.orElseThrow(() -> new DetailException(CouponConstant.E601_COUPON_CODE_NOT_FOUND));

			if (!coupon.isValid()) {
				throw new DetailException(CouponConstant.E626_COUPON_VALIDATION_FAILED);
			}

			if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
				throw new DetailException(CouponConstant.E623_COUPON_USAGE_LIMIT_REACHED);
			}

			coupon.setUsedCount(coupon.getUsedCount() + 1);
			coupon.setUpdatedAt(new Date());

			couponRepository.save(coupon);
			log.info("Đã sử dụng coupon: {}, Usage count: {} - took: {}ms", code,
					coupon.getUsedCount(), System.currentTimeMillis() - start);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi sử dụng coupon: {}", code, e);
			throw new DetailException(CouponConstant.E625_COUPON_APPLY_FAILED);
		}
	}

	// Additional methods for controller compatibility

	@Override
	public Page<CouponDTO> findAll(Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy danh sách coupon với pagination: {}", pageable);

			Page<Coupon> coupons = couponRepository.findAllData(pageable);
			log.info("Lấy {} coupons (page {}) thành công - took: {}ms",
					coupons.getNumberOfElements(), pageable.getPageNumber(),
					System.currentTimeMillis() - start);
			return coupons.map(this::convertEntityToDto);
		} catch (Exception e) {
			log.error("Lỗi khi lấy danh sách coupon", e);
			throw new DetailException(CouponConstant.E602_COUPON_FETCH_FAILED);
		}
	}

	@Override
	public CouponDTO findById(Long id) throws DetailException {
		return getCouponById(id);
	}

	@Override
	public CouponDTO findByCode(String code) throws DetailException {
		return getCouponByCode(code);
	}

	@Override
	public CouponDTO save(CouponDTO couponDTO) throws DetailException {
		return createCoupon(couponDTO);
	}

	@Override
	public CouponDTO update(CouponDTO couponDTO) throws DetailException {
		return updateCoupon(couponDTO.getId(), couponDTO);
	}

	@Override
	public void deleteById(Long id) throws DetailException {
		deleteCoupon(id);
	}

	@Override
	@Transactional(readOnly = true)
	public CouponDTO validateCoupon(String code) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Validate và lấy thông tin coupon: {}", code);

			if (code == null || code.trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code.trim(), new Date());
			if (couponOpt.isEmpty()) {
				throw new DetailException(CouponConstant.E620_COUPON_EXPIRED);
			}

			Coupon coupon = couponOpt.get();
			if (!coupon.isValid()) {
				throw new DetailException(CouponConstant.E626_COUPON_VALIDATION_FAILED);
			}

			log.info("Validate coupon {} thành công - took: {}ms", code,
					System.currentTimeMillis() - start);
			return convertEntityToDto(coupon);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi validate coupon: {}", code, e);
			throw new DetailException(CouponConstant.E626_COUPON_VALIDATION_FAILED);
		}
	}

	@Override
	@Transactional
	public CouponDTO toggleActiveStatus(Long id) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Thay đổi trạng thái hoạt động của coupon ID: {}", id);

			if (id == null || id <= 0) {
				throw new DetailException(CouponConstant.E610_INVALID_COUPON_ID);
			}

			Coupon coupon = couponRepository.findById(id)
					.orElseThrow(() -> new DetailException(CouponConstant.E600_COUPON_NOT_FOUND));

			coupon.setActive(!coupon.isActive());
			coupon.setUpdatedAt(new Date());

			Coupon savedCoupon = couponRepository.save(coupon);
			log.info("Đã thay đổi trạng thái coupon ID: {} thành: {} - took: {}ms", id,
					savedCoupon.isActive(), System.currentTimeMillis() - start);

			return convertEntityToDto(savedCoupon);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi thay đổi trạng thái coupon ID: {}", id, e);
			throw new DetailException(CouponConstant.E635_COUPON_TOGGLE_STATUS_FAILED);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getCouponStatistics(Long id) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy thống kê sử dụng coupon ID: {}", id);

			if (id == null || id <= 0) {
				throw new DetailException(CouponConstant.E610_INVALID_COUPON_ID);
			}

			Coupon coupon = couponRepository.findById(id)
					.orElseThrow(() -> new DetailException(CouponConstant.E600_COUPON_NOT_FOUND));

			Map<String, Object> statistics = new HashMap<>();

			int totalUsage = coupon.getUsedCount();
			int remainingUsage = coupon.getUsageLimit() != null ? Math.max(0, coupon.getUsageLimit() - totalUsage) : -1;

			// Calculate total savings (approximation based on average usage)
			double averageOrderValue = 100000.0; // This should be calculated from actual orders
			double totalSavings = totalUsage
					* coupon.calculateDiscount(BigDecimal.valueOf(averageOrderValue)).doubleValue();

			statistics.put("totalUsage", totalUsage);
			statistics.put("remainingUsage", remainingUsage);
			statistics.put("usageLimit", coupon.getUsageLimit());
			statistics.put("totalSavings", totalSavings);
			statistics.put("isActive", coupon.isActive());
			statistics.put("validFrom", coupon.getStartDate());
			statistics.put("validUntil", coupon.getEndDate());
			statistics.put("isExpired", new Date().after(coupon.getEndDate()));
			statistics.put("isValid", coupon.isValid());

			log.info("Lấy thống kê coupon ID: {} thành công - took: {}ms", id,
					System.currentTimeMillis() - start);
			return statistics;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy thống kê coupon ID: {}", id, e);
			throw new DetailException(CouponConstant.E631_COUPON_STATISTICS_FAILED);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> applyCoupon(String code, Double orderAmount) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Áp dụng coupon: {} cho đơn hàng: {}", code, orderAmount);

			if (code == null || code.trim().isEmpty()) {
				throw new DetailException(CouponConstant.E611_INVALID_COUPON_CODE);
			}

			if (orderAmount == null || orderAmount <= 0) {
				throw new DetailException(CouponConstant.E618_INVALID_ORDER_AMOUNT);
			}

			Map<String, Object> result = new HashMap<>();

			if (!validateCoupon(code.trim(), orderAmount)) {
				result.put("valid", false);
				result.put("message", "Mã coupon không hợp lệ hoặc không đáp ứng điều kiện");
				result.put("discountAmount", 0.0);
				result.put("finalAmount", orderAmount);
				log.info("Áp dụng coupon {} thất bại - took: {}ms", code,
						System.currentTimeMillis() - start);
				return result;
			}

			Double discountAmount = calculateDiscount(code.trim(), orderAmount);
			Double finalAmount = orderAmount - discountAmount;

			CouponDTO coupon = validateCoupon(code.trim());

			result.put("valid", true);
			result.put("message", "Áp dụng coupon thành công");
			result.put("discountAmount", discountAmount);
			result.put("finalAmount", finalAmount);
			result.put("coupon", coupon);

			log.info("Áp dụng coupon {} thành công, discount: {} - took: {}ms", code,
					discountAmount, System.currentTimeMillis() - start);
			return result;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi áp dụng coupon: {}", code, e);
			throw new DetailException(CouponConstant.E625_COUPON_APPLY_FAILED);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<CouponDTO> findActiveCoupons() throws DetailException {
		return getActiveCoupons();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CouponDTO> searchCoupons(String keyword, Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Tìm kiếm coupon với keyword: {}", keyword);

			// For now, we'll implement basic search. You can enhance this with custom
			// repository methods
			Page<Coupon> coupons = couponRepository.findAll(pageable);

			if (keyword != null && !keyword.trim().isEmpty()) {
				String lowerKeyword = keyword.toLowerCase().trim();
				coupons = coupons.map(coupon -> {
					if (coupon.getCode().toLowerCase().contains(lowerKeyword)
							|| (coupon.getName() != null && coupon.getName().toLowerCase().contains(lowerKeyword))
							|| (coupon.getDescription() != null
									&& coupon.getDescription().toLowerCase().contains(lowerKeyword))) {
						return coupon;
					}
					return null;
				});
			}

			log.info("Tìm kiếm coupon với keyword '{}' tìm thấy {} kết quả - took: {}ms",
					keyword, coupons.getNumberOfElements(), System.currentTimeMillis() - start);
			return coupons.map(this::convertEntityToDto);
		} catch (Exception e) {
			log.error("Lỗi khi tìm kiếm coupon với keyword: {}", keyword, e);
			throw new DetailException(CouponConstant.E640_COUPON_SEARCH_FAILED);
		}
	}

	// Utility methods for entity-DTO conversion

	private CouponDTO convertEntityToDto(Coupon coupon) {
		CouponDTO dto = new CouponDTO();
		dto.setId(coupon.getId());
		dto.setCode(coupon.getCode());
		dto.setName(coupon.getName());
		dto.setDescription(coupon.getDescription());
		dto.setDiscountType(coupon.getDiscountType());
		dto.setDiscountValue(coupon.getDiscountValue());

		dto.setMinOrderAmount(coupon.getMinOrderAmount());
		dto.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
		dto.setUsageLimit(coupon.getUsageLimit());
		dto.setUsedCount(coupon.getUsedCount());
		dto.setIsActive(coupon.isActive());
		dto.setStartDate(coupon.getStartDate());
		dto.setEndDate(coupon.getEndDate());
		dto.setCreatedAt(coupon.getCreatedAt());
		dto.setUpdatedAt(coupon.getUpdatedAt());

		return dto;
	}

	private Coupon convertDtoToEntity(CouponDTO dto) {
		Coupon coupon = new Coupon();
		coupon.setId(dto.getId());
		coupon.setCode(dto.getCode());
		coupon.setName(dto.getName());
		coupon.setDescription(dto.getDescription());
		coupon.setDiscountType(dto.getDiscountType());

		// Handle discountValue
		if (dto.getDiscountValue() != null) {
			coupon.setDiscountValue(dto.getDiscountValue());
		}

		coupon.setMinOrderAmount(dto.getMinOrderAmount());
		coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
		coupon.setUsageLimit(dto.getUsageLimit());
		coupon.setUsedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0);
		coupon.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
		coupon.setStartDate(dto.getStartDate());
		coupon.setEndDate(dto.getEndDate());

		return coupon;
	}
}
