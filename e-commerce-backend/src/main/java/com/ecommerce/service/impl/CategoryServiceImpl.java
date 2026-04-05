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

			Category category = convertToEntity(categoryDTO);
			category.setCreatedAt(new Date());
			category.setUpdatedAt(new Date());
			category.setActive(true);

			Category savedCategory = categoryRepository.save(category);
			log.info("Created category with ID: {}", savedCategory.getId());

			return convertToDTO(savedCategory);
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

			existingCategory.setName(categoryDTO.getName());
			existingCategory.setDescription(categoryDTO.getDescription());
			existingCategory.setImageUrl(categoryDTO.getImageUrl());
			existingCategory.setUpdatedAt(new Date());

			Category updatedCategory = categoryRepository.save(existingCategory);
			log.info("Updated category with ID: {}", updatedCategory.getId());

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
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new DetailException(CategoryConstant.E400_CATEGORY_NOT_FOUND));

			// Check if category has associated products
			// if (category.getProducts() != null && !category.getProducts().isEmpty()) {
			// throw new DetailException(CategoryConstant.E425_CATEGORY_HAS_PRODUCTS);
			// }

			categoryRepository.delete(category);
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

			category.setActive(!category.isActive());
			category.setUpdatedAt(new Date());

			Category updatedCategory = categoryRepository.save(category);
			log.info("Toggled active status for category ID: {} to {}", id, updatedCategory.isActive());

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
