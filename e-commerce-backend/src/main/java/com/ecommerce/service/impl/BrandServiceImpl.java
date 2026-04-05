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

import com.ecommerce.constant.BrandConstant;
import com.ecommerce.dto.BrandDTO;
import com.ecommerce.entity.Brand;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.service.BrandService;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of BrandService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * author: LeTuBac
 */
public class BrandServiceImpl implements BrandService {

	private static final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

	private final BrandRepository brandRepository;

	@Override
	public List<BrandDTO> findActiveBrands() throws DetailException {
		log.debug("Fetching active brands");
		try {
			List<Brand> brands = brandRepository.findByActiveTrue();
			return brands.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error fetching active brands", e);
			throw new DetailException(BrandConstant.E204_BRAND_LIST_ERROR);
		}
	}

	@Override

	public List<BrandDTO> findAll() throws DetailException {
		log.debug("Fetching all brands");
		try {
			List<Brand> brands = brandRepository.findAllData();
			return brands.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error fetching all brands", e);
			throw new DetailException(BrandConstant.E204_BRAND_LIST_ERROR);
		}
	}

	@Override

	public BrandDTO findById(Long id) throws DetailException {
		log.debug("Fetching brand with ID: {}", id);
		try {
			Brand brand = brandRepository.findById(id);
			if (brand == null) {
				throw new DetailException(BrandConstant.E200_BRAND_NOT_FOUND);
			}
			return convertToDTO(brand);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching brand with ID: {}", id, e);
			throw new DetailException(BrandConstant.E204_BRAND_LIST_ERROR);
		}
	}

	@Override
	public BrandDTO save(BrandDTO brandDTO) throws DetailException {
		log.debug("Creating new brand: {}", brandDTO.getName());

		try {
			// Validate brand name
			if (brandDTO.getName() == null || brandDTO.getName().trim().isEmpty()) {
				throw new DetailException(BrandConstant.E206_BRAND_NAME_REQUIRED);
			}

			// Check if brand name already exists
			if (brandRepository.existsByNameIgnoreCase(brandDTO.getName())) {
				throw new DetailException(BrandConstant.E205_BRAND_NAME_EXISTS);
			}

			Brand brand = convertToEntity(brandDTO);
			brand.setCreatedAt(new Date());
			brand.setUpdatedAt(new Date());
			brand.setActive(true);

			Brand savedBrand = brandRepository.save(brand);
			log.info("Created brand with ID: {}", savedBrand.getId());

			return convertToDTO(savedBrand);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating brand", e);
			throw new DetailException(BrandConstant.E201_BRAND_CREATE_ERROR);
		}
	}

	@Override
	public BrandDTO update(BrandDTO brandDTO) throws DetailException {
		log.debug("Updating brand with ID: {}", brandDTO.getId());

		try {
			// Validate brand name
			if (brandDTO.getName() == null || brandDTO.getName().trim().isEmpty()) {
				throw new DetailException(BrandConstant.E206_BRAND_NAME_REQUIRED);
			}

			Brand existingBrand = brandRepository.findById(brandDTO.getId());
			if (existingBrand == null) {
				throw new DetailException(BrandConstant.E200_BRAND_NOT_FOUND);
			}

			// Check if new name conflicts with other brands
			if (!existingBrand.getName().equalsIgnoreCase(brandDTO.getName())
					&& brandRepository.existsByNameIgnoreCase(brandDTO.getName())) {
				throw new DetailException(BrandConstant.E205_BRAND_NAME_EXISTS);
			}

			existingBrand.setName(brandDTO.getName());
			existingBrand.setDescription(brandDTO.getDescription());
			existingBrand.setImageUrl(brandDTO.getImageUrl());
			existingBrand.setWebsiteUrl(brandDTO.getWebsiteUrl());
			existingBrand.setUpdatedAt(new Date());

			Brand updatedBrand = brandRepository.save(existingBrand);
			log.info("Updated brand with ID: {}", updatedBrand.getId());

			return convertToDTO(updatedBrand);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating brand with ID: {}", brandDTO.getId(), e);
			throw new DetailException(BrandConstant.E202_BRAND_UPDATE_ERROR);
		}
	}

	@Override
	public void deleteById(Long id) throws DetailException {
		log.debug("Deleting brand with ID: {}", id);

		try {
			Brand brand = brandRepository.findById(id);
			if (brand == null) {
				throw new DetailException(BrandConstant.E200_BRAND_NOT_FOUND);
			}

			// Check if brand has associated products
			// if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
			// throw new DetailException(BrandConstant.E208_BRAND_HAS_PRODUCTS);
			// }

			brandRepository.delete(brand);
			log.info("Deleted brand with ID: {}", id);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error deleting brand with ID: {}", id, e);
			throw new DetailException(BrandConstant.E203_BRAND_DELETE_ERROR);
		}
	}

	@Override

	public boolean existsById(Long id) {
		return brandRepository.existsById(id);
	}

	@Override
	public BrandDTO toggleActiveStatus(Long id) throws DetailException {
		log.debug("Toggling active status for brand ID: {}", id);

		try {
			Brand brand = brandRepository.findById(id);
			if (brand == null) {
				throw new DetailException(BrandConstant.E200_BRAND_NOT_FOUND);
			}

			brand.setActive(!brand.isActive());
			brand.setUpdatedAt(new Date());

			Brand updatedBrand = brandRepository.save(brand);
			log.info("Toggled active status for brand ID: {} to {}", id, updatedBrand.isActive());

			return convertToDTO(updatedBrand);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error toggling brand status for ID: {}", id, e);
			throw new DetailException(BrandConstant.E210_BRAND_TOGGLE_STATUS_ERROR);
		}
	}

	@Override

	public Page<BrandDTO> search(String keyword, Pageable pageable) throws DetailException {
		log.debug("Searching brands with keyword: {}", keyword);
		try {
			Page<Brand> brands = brandRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
					keyword,
					pageable);
			return brands.map(this::convertToDTO);
		} catch (Exception e) {
			log.error("Error searching brands with keyword: {}", keyword, e);
			throw new DetailException(BrandConstant.E220_BRAND_SEARCH_ERROR);
		}
	}

	@Override

	public Page<BrandDTO> findAll(Pageable pageable) throws DetailException {
		log.debug("Fetching brands with pagination");
		try {
			Page<Brand> brands = brandRepository.findAll(pageable);
			return brands.map(this::convertToDTO);
		} catch (Exception e) {
			log.error("Error fetching brands with pagination", e);
			throw new DetailException(BrandConstant.E204_BRAND_LIST_ERROR);
		}
	}

	@Override

	public Map<String, Object> getBrandStatistics(Long brandId) throws DetailException {
		log.debug("Getting statistics for brand ID: {}", brandId);

		try {
			Brand brand = brandRepository.findById(brandId);
			if (brand == null) {
				throw new DetailException(BrandConstant.E200_BRAND_NOT_FOUND);
			}

			// int productCount = brand.getProducts() != null ? brand.getProducts().size() :
			// 0;
			int productCount = 1;

			// Mock statistics - in real implementation, calculate from actual data
			return Map.of("brandId", brandId, "brandName", brand.getName(), "totalProducts", productCount,
					"activeProducts",
					(int) (productCount * 0.9), // Mock: 90% active
					"totalSales", productCount * 1000000, // Mock sales data
					"averageRating", 4.2 + (brandId % 10) * 0.1 // Mock rating
			);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error getting statistics for brand ID: {}", brandId, e);
			throw new DetailException(BrandConstant.E221_BRAND_STATISTICS_ERROR);
		}
	}

	@Override

	public List<BrandDTO> getPopularBrands(int limit) throws DetailException {
		log.debug("Fetching {} popular brands", limit);

		try {
			// For now, return active brands ordered by name
			// In real implementation, this would consider sales, views, etc.
			List<Brand> brands = brandRepository.findByActiveTrue();

			return brands.stream().limit(limit).map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error fetching popular brands", e);
			throw new DetailException(BrandConstant.E204_BRAND_LIST_ERROR);
		}
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
