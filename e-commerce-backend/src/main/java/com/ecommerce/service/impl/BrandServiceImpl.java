package com.ecommerce.service.impl;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.BrandDTO;
import com.ecommerce.entity.Brand;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.service.BrandService;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of BrandService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {

	private static final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

	private final BrandRepository brandRepository;

	@Override
	public List<BrandDTO> findActiveBrands() {
		log.debug("Fetching active brands");
		List<Brand> brands = brandRepository.findByActiveTrue();
		return brands.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override

	public List<BrandDTO> findAll() {
		log.debug("Fetching all brands");
		List<Brand> brands = brandRepository.findAllData();
		return brands.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override

	public BrandDTO findById(Long id) {
		log.debug("Fetching brand with ID: {}", id);
		Brand brand = brandRepository.findById(id);
		if (brand == null) {
			throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
		}
		return convertToDTO(brand);
	}

	@Override
	public BrandDTO save(BrandDTO brandDTO) {
		log.debug("Creating new brand: {}", brandDTO.getName());

		// Check if brand name already exists
		if (brandRepository.existsByNameIgnoreCase(brandDTO.getName())) {
			throw new BadRequestException("Tên thương hiệu đã tồn tại");
		}

		Brand brand = convertToEntity(brandDTO);
		brand.setCreatedAt(new Date());
		brand.setUpdatedAt(new Date());
		brand.setActive(true);

		Brand savedBrand = brandRepository.save(brand);
		log.info("Created brand with ID: {}", savedBrand.getId());

		return convertToDTO(savedBrand);
	}

	@Override
	public BrandDTO update(BrandDTO brandDTO) {
		log.debug("Updating brand with ID: {}", brandDTO.getId());

		Brand existingBrand = brandRepository.findById(brandDTO.getId());
		if (existingBrand == null) {
			throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + brandDTO.getId());
		}

		// Check if new name conflicts with other brands
		if (!existingBrand.getName().equalsIgnoreCase(brandDTO.getName())
				&& brandRepository.existsByNameIgnoreCase(brandDTO.getName())) {
			throw new BadRequestException("Tên thương hiệu đã tồn tại");
		}

		existingBrand.setName(brandDTO.getName());
		existingBrand.setDescription(brandDTO.getDescription());
		existingBrand.setImageUrl(brandDTO.getImageUrl());
		existingBrand.setWebsiteUrl(brandDTO.getWebsiteUrl());
		existingBrand.setUpdatedAt(new Date());

		Brand updatedBrand = brandRepository.save(existingBrand);
		log.info("Updated brand with ID: {}", updatedBrand.getId());

		return convertToDTO(updatedBrand);
	}

	@Override
	public void deleteById(Long id) {
		log.debug("Deleting brand with ID: {}", id);

		Brand brand = brandRepository.findById(id);
		if (brand == null) {
			throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
		}

		// Check if brand has associated products
		// if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
		// throw new IllegalStateException("Không thể xóa thương hiệu có sản phẩm");
		// }

		brandRepository.delete(brand);
		log.info("Deleted brand with ID: {}", id);
	}

	@Override

	public boolean existsById(Long id) {
		return brandRepository.existsById(id);
	}

	@Override
	public BrandDTO toggleActiveStatus(Long id) {
		log.debug("Toggling active status for brand ID: {}", id);

		Brand brand = brandRepository.findById(id);
		if (brand == null) {
			throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
		}

		brand.setActive(!brand.isActive());
		brand.setUpdatedAt(new Date());

		Brand updatedBrand = brandRepository.save(brand);
		log.info("Toggled active status for brand ID: {} to {}", id, updatedBrand.isActive());

		return convertToDTO(updatedBrand);
	}

	@Override

	public Page<BrandDTO> search(String keyword, Pageable pageable) {
		log.debug("Searching brands with keyword: {}", keyword);
		Page<Brand> brands = brandRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword,
				pageable);
		return brands.map(this::convertToDTO);
	}

	@Override

	public Page<BrandDTO> findAll(Pageable pageable) {
		log.debug("Fetching brands with pagination");
		Page<Brand> brands = brandRepository.findAll(pageable);
		return brands.map(this::convertToDTO);
	}

	@Override

	public Map<String, Object> getBrandStatistics(Long brandId) {
		log.debug("Getting statistics for brand ID: {}", brandId);

		Brand brand = brandRepository.findById(brandId);
		if (brand == null) {
			throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + brandId);
		}

		// int productCount = brand.getProducts() != null ? brand.getProducts().size() :
		// 0;
		int productCount = 1;

		// Mock statistics - in real implementation, calculate from actual data
		return Map.of("brandId", brandId, "brandName", brand.getName(), "totalProducts", productCount, "activeProducts",
				(int) (productCount * 0.9), // Mock: 90% active
				"totalSales", productCount * 1000000, // Mock sales data
				"averageRating", 4.2 + (brandId % 10) * 0.1 // Mock rating
		);
	}

	@Override

	public List<BrandDTO> getPopularBrands(int limit) {
		log.debug("Fetching {} popular brands", limit);

		// For now, return active brands ordered by name
		// In real implementation, this would consider sales, views, etc.
		List<Brand> brands = brandRepository.findByActiveTrue();

		return brands.stream().limit(limit).map(this::convertToDTO).collect(Collectors.toList());
	}

	/**
	 * Convert Brand entity to BrandDTO
	 */
	private BrandDTO convertToDTO(Brand brand) {
		BrandDTO dto = new BrandDTO();
		dto.setId(brand.getId());
		dto.setName(brand.getName());
		dto.setSlug(brand.getSlug());
		dto.setDescription(brand.getDescription());
		dto.setImageUrl(brand.getImageUrl());
		dto.setWebsiteUrl(brand.getWebsiteUrl());
		dto.setActive(brand.isActive());
		dto.setCreatedAt(brand.getCreatedAt());
		dto.setUpdatedAt(brand.getUpdatedAt());

		// Set product count if available
		// if (brand.getProducts() != null) {
		// dto.setProductCount(brand.getProducts().size());
		// }

		return dto;
	}

	/**
	 * Convert BrandDTO to Brand entity
	 */
	private Brand convertToEntity(BrandDTO dto) {
		Brand brand = new Brand();
		brand.setId(dto.getId());
		brand.setName(dto.getName());
		brand.setSlug(dto.getSlug());
		brand.setDescription(dto.getDescription());
		brand.setImageUrl(dto.getImageUrl());
		brand.setWebsiteUrl(dto.getWebsiteUrl());
		brand.setActive(dto.isActive());
		return brand;
	}
}
