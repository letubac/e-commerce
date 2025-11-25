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

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CategoryService;

/**
 * Implementation of CategoryService
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

	private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

	private final CategoryRepository categoryRepository;

	// @Autowired - removed for Lombok
	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
	public List<CategoryDTO> findActiveCategories() {
		log.debug("Fetching active categories");
		List<Category> categories = categoryRepository.findByActiveTrue();
		return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public List<CategoryDTO> findAll() {
		log.debug("Fetching all categories");
		List<Category> categories = categoryRepository.findAllData();
		return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public CategoryDTO findById(Long id) {
		log.debug("Fetching category with ID: {}", id);
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
		return convertToDTO(category);
	}

	@Override
	public CategoryDTO save(CategoryDTO categoryDTO) {
		log.debug("Creating new category: {}", categoryDTO.getName());

		// Check if category name already exists
		if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
			throw new BadRequestException("Tên danh mục đã tồn tại");
		}

		Category category = convertToEntity(categoryDTO);
		category.setCreatedAt(new Date());
		category.setUpdatedAt(new Date());
		category.setActive(true);

		Category savedCategory = categoryRepository.save(category);
		log.info("Created category with ID: {}", savedCategory.getId());

		return convertToDTO(savedCategory);
	}

	@Override
	public CategoryDTO update(CategoryDTO categoryDTO) {
		log.debug("Updating category with ID: {}", categoryDTO.getId());

		Category existingCategory = categoryRepository.findById(categoryDTO.getId()).orElseThrow(
				() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryDTO.getId()));

		// Check if new name conflicts with other categories
		if (!existingCategory.getName().equalsIgnoreCase(categoryDTO.getName())
				&& categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
			throw new BadRequestException("Tên danh mục đã tồn tại");
		}

		existingCategory.setName(categoryDTO.getName());
		existingCategory.setDescription(categoryDTO.getDescription());
		existingCategory.setImageUrl(categoryDTO.getImageUrl());
		existingCategory.setUpdatedAt(new Date());

		Category updatedCategory = categoryRepository.save(existingCategory);
		log.info("Updated category with ID: {}", updatedCategory.getId());

		return convertToDTO(updatedCategory);
	}

	@Override
	public void deleteById(Long id) {
		log.debug("Deleting category with ID: {}", id);

		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

		// Check if category has associated products
		// if (category.getProducts() != null && !category.getProducts().isEmpty()) {
		// throw new IllegalStateException("Không thể xóa danh mục có sản phẩm");
		// }

		categoryRepository.delete(category);
		log.info("Deleted category with ID: {}", id);
	}

	@Override
	public boolean existsById(Long id) {
		return categoryRepository.existsById(id);
	}

	@Override
	public CategoryDTO toggleActiveStatus(Long id) {
		log.debug("Toggling active status for category ID: {}", id);

		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

		category.setActive(!category.isActive());
		category.setUpdatedAt(new Date());

		Category updatedCategory = categoryRepository.save(category);
		log.info("Toggled active status for category ID: {} to {}", id, updatedCategory.isActive());

		return convertToDTO(updatedCategory);
	}

	@Override
	public Page<CategoryDTO> search(String keyword, Pageable pageable) {
		log.debug("Searching categories with keyword: {}", keyword);
		Page<Category> categories = categoryRepository
				.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, pageable);
		return categories.map(this::convertToDTO);
	}

	@Override
	public Page<CategoryDTO> findAll(Pageable pageable) {
		log.debug("Fetching categories with pagination");
		Page<Category> categories = categoryRepository.findAll(pageable);
		return categories.map(this::convertToDTO);
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
