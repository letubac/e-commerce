package com.ecommerce.service.impl;

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.entity.Coupon;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.BadRequestException;
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
	public CouponDTO createCoupon(CouponDTO couponDTO) {
		log.debug("Tạo coupon mới với code: {}", couponDTO.getCode());

		// Validate coupon code uniqueness
		if (couponRepository.existsByCode(couponDTO.getCode())) {
			throw new BadRequestException("Mã coupon đã tồn tại");
		}

		// Validate dates
		if (couponDTO.getStartDate() != null && couponDTO.getEndDate() != null) {
			if (couponDTO.getStartDate().after(couponDTO.getEndDate())) {
				throw new BadRequestException("Ngày bắt đầu không thể sau ngày kết thúc");
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

		log.info("Đã tạo coupon thành công với ID: {}, Code: {}", couponId, couponDTO.getCode());

		// Retrieve and return the saved coupon
		Coupon savedCoupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new RuntimeException("Lỗi khi lưu coupon"));

		return convertEntityToDto(savedCoupon);
	}

	@Override
	public CouponDTO updateCoupon(Long id, CouponDTO couponDTO) {
		log.debug("Cập nhật coupon ID: {}", id);

		Coupon existingCoupon = couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với ID: " + id));

		// Check if code is being changed and if new code already exists
		if (!existingCoupon.getCode().equals(couponDTO.getCode())
				&& couponRepository.existsByCode(couponDTO.getCode())) {
			throw new BadRequestException("Mã coupon đã tồn tại");
		}

		// Validate dates
		if (couponDTO.getStartDate() != null && couponDTO.getEndDate() != null) {
			if (couponDTO.getStartDate().after(couponDTO.getEndDate())) {
				throw new BadRequestException("Ngày bắt đầu không thể sau ngày kết thúc");
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

		log.info("Đã cập nhật coupon thành công với ID: {}", id);

		// Retrieve and return updated coupon
		Coupon updatedCoupon = couponRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Lỗi khi cập nhật coupon"));

		return convertEntityToDto(updatedCoupon);
	}

	@Override
	public void deleteCoupon(Long id) {
		log.debug("Xóa coupon ID: {}", id);

		if (!couponRepository.existsById(id)) {
			throw new ResourceNotFoundException("Không tìm thấy coupon với ID: " + id);
		}

		couponRepository.deleteById(id);
		log.info("Đã xóa coupon thành công với ID: {}", id);
	}

	@Override
	public CouponDTO getCouponById(Long id) {
		log.debug("Lấy coupon theo ID: {}", id);

		Coupon coupon = couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với ID: " + id));

		return convertEntityToDto(coupon);
	}

	@Override
	public CouponDTO getCouponByCode(String code) {
		log.debug("Lấy coupon theo code: {}", code);

		Coupon coupon = couponRepository.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với code: " + code));

		return convertEntityToDto(coupon);
	}

	@Override
	public List<CouponDTO> getAllCoupons() {
		log.debug("Lấy tất cả coupon");

		List<Coupon> coupons = couponRepository.findAllData();
		return coupons.stream().map(this::convertEntityToDto).collect(Collectors.toList());
	}

	@Override
	public List<CouponDTO> getActiveCoupons() {
		log.debug("Lấy danh sách coupon đang hoạt động");

		List<Coupon> coupons = couponRepository.findValidCoupons(new Date());
		return coupons.stream().map(this::convertEntityToDto).collect(Collectors.toList());
	}

	@Override
	public boolean validateCoupon(String code, Double orderAmount) {
		log.debug("Validate coupon: {} cho đơn hàng: {}", code, orderAmount);

		try {
			Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code, new Date());
			if (couponOpt.isEmpty()) {
				return false;
			}

			Coupon coupon = couponOpt.get();
			return coupon.isValid()
					&& (coupon.getMinOrderAmount() == null || orderAmount >= coupon.getMinOrderAmount().doubleValue());
		} catch (Exception e) {
			log.error("Lỗi khi validate coupon: {}", code, e);
			return false;
		}
	}

	@Override
	public Double calculateDiscount(String code, Double orderAmount) {
		log.debug("Tính discount cho coupon: {} với order amount: {}", code, orderAmount);

		try {
			Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code, new Date());
			if (couponOpt.isEmpty()) {
				return 0.0;
			}

			Coupon coupon = couponOpt.get();
			return coupon.calculateDiscount(BigDecimal.valueOf(orderAmount)).doubleValue();
		} catch (Exception e) {
			log.error("Lỗi khi tính discount cho coupon: {}", code, e);
			return 0.0;
		}
	}

	@Override
	public void useCoupon(String code) {
		log.debug("Sử dụng coupon: {}", code);

		Coupon coupon = couponRepository.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với code: " + code));

		if (!coupon.isValid()) {
			throw new IllegalStateException("Coupon không còn hợp lệ");
		}

		coupon.setUsedCount(coupon.getUsedCount() + 1);
		coupon.setUpdatedAt(new Date());

		couponRepository.save(coupon);
		log.info("Đã sử dụng coupon: {}, Usage count: {}", code, coupon.getUsedCount());
	}

	// Additional methods for controller compatibility

	@Override
	public Page<CouponDTO> findAll(Pageable pageable) {
		log.debug("Lấy danh sách coupon với pagination: {}", pageable);

		Page<Coupon> coupons = couponRepository.findAllData(pageable);
		return coupons.map(this::convertEntityToDto);
	}

	@Override
	public CouponDTO findById(Long id) {
		return getCouponById(id);
	}

	@Override
	public CouponDTO findByCode(String code) {
		return getCouponByCode(code);
	}

	@Override
	public CouponDTO save(CouponDTO couponDTO) {
		return createCoupon(couponDTO);
	}

	@Override
	public CouponDTO update(CouponDTO couponDTO) {
		return updateCoupon(couponDTO.getId(), couponDTO);
	}

	@Override
	public void deleteById(Long id) {
		deleteCoupon(id);
	}

	@Override
	@Transactional(readOnly = true)
	public CouponDTO validateCoupon(String code) {
		log.debug("Validate và lấy thông tin coupon: {}", code);

		Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code, new Date());
		if (couponOpt.isEmpty()) {
			throw new ResourceNotFoundException("Mã coupon không hợp lệ hoặc đã hết hạn");
		}

		Coupon coupon = couponOpt.get();
		if (!coupon.isValid()) {
			throw new ResourceNotFoundException("Mã coupon không còn hiệu lực");
		}

		return convertEntityToDto(coupon);
	}

	@Override
	@Transactional
	public CouponDTO toggleActiveStatus(Long id) {
		log.debug("Thay đổi trạng thái hoạt động của coupon ID: {}", id);

		Coupon coupon = couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với ID: " + id));

		coupon.setActive(!coupon.isActive());
		coupon.setUpdatedAt(new Date());

		Coupon savedCoupon = couponRepository.save(coupon);
		log.info("Đã thay đổi trạng thái coupon ID: {} thành: {}", id, savedCoupon.isActive());

		return convertEntityToDto(savedCoupon);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getCouponStatistics(Long id) {
		log.debug("Lấy thống kê sử dụng coupon ID: {}", id);

		Coupon coupon = couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với ID: " + id));

		Map<String, Object> statistics = new HashMap<>();

		int totalUsage = coupon.getUsedCount();
		int remainingUsage = coupon.getUsageLimit() != null ? Math.max(0, coupon.getUsageLimit() - totalUsage) : -1; // -1
																														// means
																														// unlimited

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

		return statistics;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> applyCoupon(String code, Double orderAmount) {
		log.debug("Áp dụng coupon: {} cho đơn hàng: {}", code, orderAmount);

		Map<String, Object> result = new HashMap<>();

		try {
			if (!validateCoupon(code, orderAmount)) {
				result.put("valid", false);
				result.put("message", "Mã coupon không hợp lệ hoặc không đáp ứng điều kiện");
				result.put("discountAmount", 0.0);
				result.put("finalAmount", orderAmount);
				return result;
			}

			Double discountAmount = calculateDiscount(code, orderAmount);
			Double finalAmount = orderAmount - discountAmount;

			CouponDTO coupon = validateCoupon(code);

			result.put("valid", true);
			result.put("message", "Áp dụng coupon thành công");
			result.put("discountAmount", discountAmount);
			result.put("finalAmount", finalAmount);
			result.put("coupon", coupon);

		} catch (Exception e) {
			log.error("Lỗi khi áp dụng coupon: {}", code, e);
			result.put("valid", false);
			result.put("message", e.getMessage());
			result.put("discountAmount", 0.0);
			result.put("finalAmount", orderAmount);
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CouponDTO> findActiveCoupons() {
		return getActiveCoupons();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CouponDTO> searchCoupons(String keyword, Pageable pageable) {
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

		return coupons.map(this::convertEntityToDto);
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
