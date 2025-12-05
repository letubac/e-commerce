package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	// Helper method to generate slug from name
	private String generateSlug(String name) {
		return name.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").trim();
	}

	@Override
	public ProductDTO createProduct(ProductDTO productDTO) {
		// Validate SKU availability
		if (!isSkuAvailable(productDTO.getSku())) {
			throw new BadRequestException("SKU is already in use: " + productDTO.getSku());
		}

		// Generate slug if not provided
		if (productDTO.getSlug() == null || productDTO.getSlug().isEmpty()) {
			String baseSlug = generateSlug(productDTO.getName());
			String finalSlug = baseSlug;
			int counter = 1;
			while (!isSlugAvailable(finalSlug)) {
				finalSlug = baseSlug + "-" + counter;
				counter++;
			}
			productDTO.setSlug(finalSlug);
		} else if (!isSlugAvailable(productDTO.getSlug())) {
			throw new BadRequestException("Slug is already in use: " + productDTO.getSlug());
		}

		Date now = new Date();

		Integer result = productRepository.insertProduct(productDTO.getName(), productDTO.getDescription(),
				productDTO.getShortDescription(), productDTO.getSku(), productDTO.getPrice(), productDTO.getSalePrice(),
				productDTO.getCostPrice(), productDTO.getStockQuantity(),
				productDTO.getLowStockThreshold() != null ? productDTO.getLowStockThreshold() : 10,
				productDTO.getWeight(), productDTO.getDimensions(), productDTO.getCategoryId(), productDTO.getBrandId(),
				productDTO.isActive(), productDTO.isFeatured(),
				productDTO.getStatus() != null ? productDTO.getStatus() : "PUBLISHED", productDTO.getMetaTitle(),
				productDTO.getMetaDescription(), productDTO.getSlug(), now, now);

		if (result == null || result <= 0) {
			throw new RuntimeException("Failed to create product");
		}

		// Retrieve the created product
		Optional<Product> createdProduct = productRepository.findBySku(productDTO.getSku());
		if (createdProduct.isEmpty()) {
			throw new RuntimeException("Failed to retrieve created product");
		}

		return productMapper.toDTO(createdProduct.get());
	}

	@Override
	public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
		Optional<Product> existingProductOpt = productRepository.findById(id);
		if (existingProductOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + id);
		}

		Product existingProduct = existingProductOpt.get();

		// Check SKU availability if SKU is being changed
		if (!existingProduct.getSku().equals(productDTO.getSku()) && !isSkuAvailable(productDTO.getSku())) {
			throw new BadRequestException("SKU is already in use: " + productDTO.getSku());
		}

		// Check slug availability if slug is being changed
		if (productDTO.getSlug() != null && !existingProduct.getSlug().equals(productDTO.getSlug())
				&& !isSlugAvailable(productDTO.getSlug())) {
			throw new BadRequestException("Slug is already in use: " + productDTO.getSlug());
		}

		Date now = new Date();

		Integer result = productRepository.updateProduct(id, productDTO.getName(), productDTO.getDescription(),
				productDTO.getPrice(), productDTO.getSalePrice(), productDTO.getStockQuantity(),
				productDTO.getCategoryId(), productDTO.getBrandId(), productDTO.getWeight(), productDTO.getDimensions(),
				productDTO.isActive(), productDTO.isFeatured(),
				productDTO.getStatus() != null ? productDTO.getStatus() : "PUBLISHED", productDTO.getMetaTitle(),
				productDTO.getMetaDescription(), now);

		if (result == null || result <= 0) {
			throw new RuntimeException("Failed to update product");
		}

		// Retrieve the updated product
		Optional<Product> updatedProduct = productRepository.findById(id);
		return productMapper.toDTO(updatedProduct.get());
	}

	@Override
	public Optional<ProductDTO> getProductById(Long id) {
		return productRepository.findById(id).map(productMapper::toDTO);
	}

	@Override
	public ProductDTO getProductByIdOrThrow(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
		if (Objects.isNull(product)) {
			return productMapper.toDTO(product);
		}
		ProductDTO productDTO = productMapper.toDTO(product);
		// Lazy load images if needed
		productDTO.setProductImages(productRepository.findImagesByProductId(product.getId()).stream()
							.map(image -> productMapper.toDtoProductImage(image)).collect(Collectors.toList()));
		return productDTO;
	}

	@Override
	public Optional<ProductDTO> getProductBySlug(String slug) {
		return productRepository.findBySlug(slug).map(productMapper::toDTO);
	}

	@Override
	public Optional<ProductDTO> getProductBySku(String sku) {
		return productRepository.findBySku(sku).map(productMapper::toDTO);
	}

	@Override
	public List<ProductDTO> getAllProducts() {
		return productRepository.findAllData().stream().map(productMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public Page<ProductDTO> getAllProducts(Pageable pageable) {
		List<ProductDTO> products = getAllProducts();
		if (products.isEmpty()) {
			return Page.empty(pageable);
		}
		// Get images product for each product
		products.forEach(product -> {
			// Lazy load images if needed
			product.setProductImages(productRepository.findImagesByProductId(product.getId()).stream()
					.map(image -> productMapper.toDtoProductImage(image)).collect(Collectors.toList()));
		});
		int start = Math.min((int) pageable.getOffset(), products.size());
		int end = Math.min((start + pageable.getPageSize()), products.size());
		List<ProductDTO> subList = products.subList(start, end);
		return new PageImpl<>(subList, pageable, products.size());
	}

	@Override
	public List<ProductDTO> getActiveProducts() {
		return productRepository.findActive().stream().map(productMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> getFeaturedProducts() {
		return productRepository.findFeatured().stream().map(productMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public void deleteProduct(Long id) {
		Optional<Product> product = productRepository.findById(id);
		if (product.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + id);
		}
		productRepository.hardDelete(id);
	}

	@Override
	public void deactivateProduct(Long id) {
		Optional<Product> productOpt = productRepository.findById(id);
		if (productOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + id);
		}

		Product product = productOpt.get();
		Date now = new Date();

		productRepository.updateProduct(id, product.getName(), product.getDescription(), product.getPrice(),
				product.getSalePrice(), product.getStockQuantity(), product.getCategoryId(), product.getBrandId(),
				product.getWeight(), product.getDimensions(), false, // Set active to false
				product.isFeatured(), product.getStatus(), product.getMetaTitle(), product.getMetaDescription(), now);
	}

	@Override
	public void activateProduct(Long id) {
		Optional<Product> productOpt = productRepository.findById(id);
		if (productOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + id);
		}

		Product product = productOpt.get();
		Date now = new Date();

		productRepository.updateProduct(id, product.getName(), product.getDescription(), product.getPrice(),
				product.getSalePrice(), product.getStockQuantity(), product.getCategoryId(), product.getBrandId(),
				product.getWeight(), product.getDimensions(), true, // Set active to true
				product.isFeatured(), product.getStatus(), product.getMetaTitle(), product.getMetaDescription(), now);
	}

	// Product filtering and search
	@Override
	public List<ProductDTO> getProductsByCategory(Long categoryId) {
		return productRepository.findByCategory(categoryId).stream().map(productMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
		List<ProductDTO> products = getProductsByCategory(categoryId);
		if (products.isEmpty()) {
			return Page.empty(pageable);
		}
		// Get images product for each product
		products.forEach(product -> {
			// Lazy load images if needed
			product.setProductImages(productRepository.findImagesByProductId(product.getId()).stream()
					.map(image -> productMapper.toDtoProductImage(image)).collect(Collectors.toList()));
		});
		int start = Math.min((int) pageable.getOffset(), products.size());
		int end = Math.min((start + pageable.getPageSize()), products.size());
		List<ProductDTO> subList = products.subList(start, end);
		return new PageImpl<>(subList, pageable, products.size());
	}

	@Override
	public List<ProductDTO> getProductsByBrand(Long brandId) {
		return productRepository.findByBrand(brandId).stream().map(productMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public Page<ProductDTO> getProductsByBrand(Long brandId, Pageable pageable) {
		List<ProductDTO> products = getProductsByBrand(brandId);
		if (products.isEmpty()) {
			return Page.empty(pageable);
		}
		// Get images product for each product
		products.forEach(product -> {
			// Lazy load images if needed
			product.setProductImages(productRepository.findImagesByProductId(product.getId()).stream()
					.map(image -> productMapper.toDtoProductImage(image)).collect(Collectors.toList()));
		});
		int start = Math.min((int) pageable.getOffset(), products.size());
		int end = Math.min((start + pageable.getPageSize()), products.size());
		List<ProductDTO> subList = products.subList(start, end);
		return new PageImpl<>(subList, pageable, products.size());
	}

	@Override
	public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
		return productRepository.findByPriceRange(minPrice, maxPrice).stream().map(productMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> searchProducts(String keyword) {
		return productRepository.searchByName(keyword).stream().map(productMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
		List<ProductDTO> products = searchProducts(keyword);
		if (products.isEmpty()) {
			return Page.empty(pageable);
		}
		// Get images product for each product
		products.forEach(product -> {
			// Lazy load images if needed
			product.setProductImages(productRepository.findImagesByProductId(product.getId()).stream()
					.map(image -> productMapper.toDtoProductImage(image)).collect(Collectors.toList()));
		});
		int start = Math.min((int) pageable.getOffset(), products.size());
		int end = Math.min((start + pageable.getPageSize()), products.size());
		List<ProductDTO> subList = products.subList(start, end);
		return new PageImpl<>(subList, pageable, products.size());
	}

	@Override
	public List<ProductDTO> getLowStockProducts() {
		return productRepository.findLowStock(10).stream() // Default threshold of 10
				.map(productMapper::toDTO).collect(Collectors.toList());
	}

	// Pagination
	@Override
	public List<ProductDTO> getProductsWithPagination(int page, int size) {
		int offset = page * size;
		return productRepository.findWithPagination(size, offset).stream().map(productMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> getActiveProductsPaginated(int page, int size) {
		List<ProductDTO> activeProducts = getActiveProducts();
		int start = Math.min(page * size, activeProducts.size());
		int end = Math.min(start + size, activeProducts.size());
		return activeProducts.subList(start, end);
	}

	@Override
	public List<ProductDTO> getProductsByCategoryPaginated(Long categoryId, int page, int size) {
		List<ProductDTO> products = getProductsByCategory(categoryId);
		int start = Math.min(page * size, products.size());
		int end = Math.min(start + size, products.size());
		return products.subList(start, end);
	}

	// Stock management
	@Override
	public void updateStock(Long productId, Integer newStock) {
		Optional<Product> product = productRepository.findById(productId);
		if (product.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		Date now = new Date();
		Integer result = productRepository.updateStock(productId, newStock, now);

		if (result == null || result <= 0) {
			throw new RuntimeException("Failed to update stock for product: " + productId);
		}
	}

	@Override
	public void increaseStock(Long productId, Integer quantity) {
		Optional<Product> productOpt = productRepository.findById(productId);
		if (productOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		Product product = productOpt.get();
		Integer newStock = product.getStockQuantity() + quantity;
		updateStock(productId, newStock);
	}

	@Override
	public void decreaseStock(Long productId, Integer quantity) {
		Optional<Product> productOpt = productRepository.findById(productId);
		if (productOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		Product product = productOpt.get();
		Integer newStock = Math.max(0, product.getStockQuantity() - quantity);
		updateStock(productId, newStock);
	}

	@Override
	public boolean isInStock(Long productId) {
		Optional<Product> product = productRepository.findById(productId);
		return product.isPresent() && product.get().isInStock();
	}

	// Price management
	@Override
	public void updatePrice(Long productId, BigDecimal newPrice) {
		Optional<Product> product = productRepository.findById(productId);
		if (product.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		Date now = new Date();
		Integer result = productRepository.updatePrice(productId, newPrice, now);

		if (result == null || result <= 0) {
			throw new RuntimeException("Failed to update price for product: " + productId);
		}
	}

	@Override
	public void setSalePrice(Long productId, BigDecimal salePrice) {
		Optional<Product> product = productRepository.findById(productId);
		if (product.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		Date now = new Date();
		Integer result = productRepository.updateSalePrice(productId, salePrice, now);

		if (result == null || result <= 0) {
			throw new RuntimeException("Failed to update sale price for product: " + productId);
		}
	}

	@Override
	public void removeSalePrice(Long productId) {
		setSalePrice(productId, null);
	}

	// Validation
	@Override
	public boolean isSkuAvailable(String sku) {
		return !productRepository.existsBySku(sku);
	}

	@Override
	public boolean isSlugAvailable(String slug) {
		return !productRepository.existsBySlug(slug);
	}

	// Statistics
	@Override
	public long getTotalProductCount() {
		return getAllProducts().size();
	}

	@Override
	public long getActiveProductCount() {
		return productRepository.countActive();
	}

	@Override
	public long getProductCountByCategory(Long categoryId) {
		return productRepository.countByCategory(categoryId);
	}

	@Override
	public long getProductCountByBrand(Long brandId) {
		return productRepository.countByBrand(brandId);
	}

	@Override
	public Page<ProductDTO> findByCategoryId(Long categoryId, Pageable pageable, Double minPrice, Double maxPrice,
			Long brandId, Boolean active) {
		// Implementation with filtering
		List<ProductDTO> products = productRepository.findByCategory(categoryId).stream().map(productMapper::toDTO)
				.filter(product -> {
					// Apply filters
					if (active != null && product.isActive() != active)
						return false;
					if (brandId != null && !brandId.equals(product.getBrandId()))
						return false;
					if (minPrice != null && product.getPrice().doubleValue() < minPrice)
						return false;
					if (maxPrice != null && product.getPrice().doubleValue() > maxPrice)
						return false;
					return true;
				}).collect(Collectors.toList());

		int start = Math.min((int) pageable.getOffset(), products.size());
		int end = Math.min((start + pageable.getPageSize()), products.size());
		List<ProductDTO> subList = products.subList(start, end);
		return new PageImpl<>(subList, pageable, products.size());
	}

	@Override
	public Page<ProductDTO> findByBrandId(Long brandId, Pageable pageable, Double minPrice, Double maxPrice,
			Long categoryId, Boolean active) {
		// Implementation with filtering
		List<ProductDTO> products = productRepository.findByBrand(brandId).stream().map(productMapper::toDTO)
				.filter(product -> {
					// Apply filters
					if (active != null && product.isActive() != active)
						return false;
					if (categoryId != null && !categoryId.equals(product.getCategoryId()))
						return false;
					if (minPrice != null && product.getPrice().doubleValue() < minPrice)
						return false;
					if (maxPrice != null && product.getPrice().doubleValue() > maxPrice)
						return false;
					return true;
				}).collect(Collectors.toList());

		int start = Math.min((int) pageable.getOffset(), products.size());
		int end = Math.min((start + pageable.getPageSize()), products.size());
		List<ProductDTO> subList = products.subList(start, end);
		return new PageImpl<>(subList, pageable, products.size());
	}
}
