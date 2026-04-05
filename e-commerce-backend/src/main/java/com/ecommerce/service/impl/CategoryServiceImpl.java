package com.ecommerce.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.constant.CategoryConstant;
import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CategoryService;

/**
 * Implementation of CategoryService
 */
@Service
@Transactional
/**
 * author: LeTuBac
 */
public class CategoryServiceImpl implements CategoryService {

	private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

	private final CategoryRepository categoryRepository;

	// @Autowired - removed for Lombok
	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
	public List<CategoryDTO> findActiveCategories() throws DetailException {
		log.debug("Fetching active categories");
		try {
			List<Category> categories = categoryRepository.findByActiveTrue();
			log.info("Found {} active categories", categories.size());
			return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error fetching active categories", e);
			throw new DetailException(CategoryConstant.E405_CATEGORY_GET_ERROR);
		}
	}

	@Override
	public List<CategoryDTO> findAll() throws DetailException {
		log.debug("Fetching all categories");
		try {
			List<Category> categories = categoryRepository.findAllData();
			log.info("Found {} categories", categories.size());
			return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error fetching all categories", e);
			throw new DetailException(CategoryConstant.E405_CATEGORY_GET_ERROR);
		}
	}

	@Override
	public CategoryDTO findById(Long id) throws DetailException {
		log.debug("Fetching category with ID: {}", id);
		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new DetailException(CategoryConstant.E400_CATEGORY_NOT_FOUND));
			log.info("Found category with ID: {}", id);
			return convertToDTO(category);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching category with ID: {}", id, e);
			throw new DetailException(CategoryConstant.E405_CATEGORY_GET_ERROR);
		}
	}

	private String generateSlug(String name) {
		return name.toLowerCase()
				.replaceAll("[àáâãäå]", "a").replaceAll("[èéêë]", "e")
				.replaceAll("[ìíîï]", "i").replaceAll("[òóôõö]", "o").replaceAll("[ùúûü]", "u")
				.replaceAll("[ăắặằẳẵ]", "a").replaceAll("[âấậầẩẫ]", "a")
				.replaceAll("[êếệềểễ]", "e").replaceAll("[ôốộồổỗ]", "o")
				.replaceAll("[ơớợờởỡ]", "o").replaceAll("[ưứựừửữ]", "u")
				.replaceAll("[ịỉĩ]", "i").replaceAll("[ụủũ]", "u")
				.replaceAll("[ạảã]", "a").replaceAll("[ẹẻẽ]", "e").replaceAll("[ọỏõ]", "o")
				.replaceAll("[đĐ]", "d")
				.replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-")
				.replaceAll("-+", "-").replaceAll("^-|-$", "");
	}

	@Override
	public CategoryDTO save(CategoryDTO categoryDTO) throws DetailException {
		log.debug("Creating new category: {}", categoryDTO.getName());

		try {
			// Validate category name
			if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
				throw new DetailException(CategoryConstant.E410_CATEGORY_NAME_REQUIRED);
			}

			// Check if category name already exists
			if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
				throw new DetailException(CategoryConstant.E411_CATEGORY_NAME_EXISTS);
			}

			Date now = new Date();
			String slug = (categoryDTO.getSlug() != null && !categoryDTO.getSlug().isBlank())
					? categoryDTO.getSlug()
					: generateSlug(categoryDTO.getName());

			Integer result = categoryRepository.insertCategory(
					categoryDTO.getName(), slug, categoryDTO.getDescription(),
					categoryDTO.getParentId(), categoryDTO.getImageUrl(),
					categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0,
					true, now, now);

			if (result == null || result <= 0) {
				throw new DetailException(CategoryConstant.E406_CATEGORY_CREATE_ERROR);
			}

			Category savedCategory = categoryRepository.findByExactName(categoryDTO.getName()).orElse(null);
			log.info("Created category with ID: {}", savedCategory != null ? savedCategory.getId() : "unknown");
			return savedCategory != null ? convertToDTO(savedCategory) : categoryDTO;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating category: {}", categoryDTO.getName(), e);
			throw new DetailException(CategoryConstant.E406_CATEGORY_CREATE_ERROR);
		}
	}

	@Override
	public CategoryDTO update(CategoryDTO categoryDTO) throws DetailException {
		log.debug("Updating category with ID: {}", categoryDTO.getId());

		try {
			// Validate category ID
			if (categoryDTO.getId() == null) {
				throw new DetailException(CategoryConstant.E414_CATEGORY_ID_REQUIRED);
			}

			// Validate category name
			if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
				throw new DetailException(CategoryConstant.E410_CATEGORY_NAME_REQUIRED);
			}

			Category existingCategory = categoryRepository.findById(categoryDTO.getId())
					.orElseThrow(() -> new DetailException(CategoryConstant.E400_CATEGORY_NOT_FOUND));

			// Check if new name conflicts with other categories
			if (!existingCategory.getName().equalsIgnoreCase(categoryDTO.getName())
					&& categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
				throw new DetailException(CategoryConstant.E411_CATEGORY_NAME_EXISTS);
			}

			String slug = (categoryDTO.getSlug() != null && !categoryDTO.getSlug().isBlank())
					? categoryDTO.getSlug()
					: existingCategory.getSlug() != null ? existingCategory.getSlug()
							: generateSlug(categoryDTO.getName());
			Date now = new Date();

			Integer result = categoryRepository.updateCategory(
					categoryDTO.getId(), categoryDTO.getName(), slug,
					categoryDTO.getDescription(), categoryDTO.getParentId(),
					categoryDTO.getImageUrl(),
					categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder()
							: (existingCategory.getSortOrder() != null ? existingCategory.getSortOrder() : 0),
					existingCategory.isActive(), now);

			if (result == null || result <= 0) {
				throw new DetailException(CategoryConstant.E407_CATEGORY_UPDATE_ERROR);
			}

			Category updatedCategory = categoryRepository.findById(categoryDTO.getId())
					.orElse(existingCategory);
			log.info("Updated category with ID: {}", categoryDTO.getId());
			return convertToDTO(updatedCategory);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating category with ID: {}", categoryDTO.getId(), e);
			throw new DetailException(CategoryConstant.E407_CATEGORY_UPDATE_ERROR);
		}
	}

	@Override
	public void deleteById(Long id) throws DetailException {
		log.debug("Deleting category with ID: {}", id);

		try {
			boolean exists = categoryRepository.existsById(id);
			if (!exists) {
				throw new DetailException(CategoryConstant.E400_CATEGORY_NOT_FOUND);
			}

			categoryRepository.deleteCategory(id);
			log.info("Deleted category with ID: {}", id);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error deleting category with ID: {}", id, e);
			throw new DetailException(CategoryConstant.E408_CATEGORY_DELETE_ERROR);
		}
	}

	@Override
	public boolean existsById(Long id) {
		return categoryRepository.existsById(id);
	}

	@Override
	public CategoryDTO toggleActiveStatus(Long id) throws DetailException {
		log.debug("Toggling active status for category ID: {}", id);

		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new DetailException(CategoryConstant.E400_CATEGORY_NOT_FOUND));

			boolean newStatus = !category.isActive();
			Date now = new Date();
			String slug = category.getSlug() != null ? category.getSlug() : generateSlug(category.getName());

			Integer result = categoryRepository.updateCategory(
					id, category.getName(), slug, category.getDescription(),
					category.getParentId(), category.getImageUrl(),
					category.getSortOrder() != null ? category.getSortOrder() : 0,
					newStatus, now);

			if (result == null || result <= 0) {
				throw new DetailException(CategoryConstant.E409_CATEGORY_TOGGLE_ERROR);
			}

			Category updatedCategory = categoryRepository.findById(id).orElse(category);
			log.info("Toggled active status for category ID: {} to {}", id, newStatus);
			return convertToDTO(updatedCategory);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error toggling active status for category ID: {}", id, e);
			throw new DetailException(CategoryConstant.E409_CATEGORY_TOGGLE_ERROR);
		}
	}

	@Override
	public Page<CategoryDTO> search(String keyword, Pageable pageable) throws DetailException {
		log.debug("Searching categories with keyword: {}", keyword);
		try {
			Page<Category> categories = categoryRepository
					.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, pageable);
			log.info("Found {} categories with keyword: {}", categories.getTotalElements(), keyword);
			return categories.map(this::convertToDTO);
		} catch (Exception e) {
			log.error("Error searching categories with keyword: {}", keyword, e);
			throw new DetailException(CategoryConstant.E430_CATEGORY_SEARCH_ERROR);
		}
	}

	@Override
	public Page<CategoryDTO> findAll(Pageable pageable) throws DetailException {
		log.debug("Fetching categories with pagination");
		try {
			Page<Category> categories = categoryRepository.findAll(pageable);
			log.info("Found {} categories (page {} of {})", categories.getTotalElements(),
					categories.getNumber(), categories.getTotalPages());
			return categories.map(this::convertToDTO);
		} catch (Exception e) {
			log.error("Error fetching categories with pagination", e);
			throw new DetailException(CategoryConstant.E431_CATEGORY_PAGINATION_ERROR);
		}
	}

	/**
	 * Convert Category entity to CategoryDTO
	 */
	private CategoryDTO convertToDTO(Category category) {
		CategoryDTO dto = new CategoryDTO();
		dto.setId(category.getId());
		dto.setName(category.getName());
		dto.setSlug(category.getSlug());
		dto.setDescription(category.getDescription());
		dto.setParentId(category.getParentId());
		dto.setImageUrl(category.getImageUrl());
		dto.setActive(category.isActive());
		dto.setSortOrder(category.getSortOrder());
		dto.setMetaTitle(category.getMetaTitle());
		dto.setMetaDescription(category.getMetaDescription());
		dto.setCreatedAt(category.getCreatedAt());
		dto.setUpdatedAt(category.getUpdatedAt());

		return dto;
	}

	/**
	 * Convert CategoryDTO to Category entity
	 */
	private Category convertToEntity(CategoryDTO dto) {
		Category category = new Category();
		category.setId(dto.getId());
		category.setName(dto.getName());
		category.setSlug(dto.getSlug());
		category.setDescription(dto.getDescription());
		category.setParentId(dto.getParentId());
		category.setImageUrl(dto.getImageUrl());
		category.setActive(dto.isActive());
		category.setSortOrder(dto.getSortOrder());
		category.setMetaTitle(dto.getMetaTitle());
		category.setMetaDescription(dto.getMetaDescription());
		return category;
	}
}
