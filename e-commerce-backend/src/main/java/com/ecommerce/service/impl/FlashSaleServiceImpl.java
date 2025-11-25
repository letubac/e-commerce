package com.ecommerce.service.impl;

import com.ecommerce.dto.FlashSaleDTO;
import com.ecommerce.dto.FlashSaleProductDTO;
import com.ecommerce.entity.FlashSale;
import com.ecommerce.entity.FlashSaleProduct;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.FlashSaleRepository;
import com.ecommerce.repository.FlashSaleProductRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlashSaleServiceImpl implements FlashSaleService {

	private final FlashSaleRepository flashSaleRepository;
	private final FlashSaleProductRepository flashSaleProductRepository;
	private final ProductRepository productRepository;

	@Override
	public FlashSaleDTO createFlashSale(FlashSaleDTO flashSaleDTO) {
		log.debug("Creating flash sale: {}", flashSaleDTO.getName());

		FlashSale flashSale = new FlashSale();
		flashSale.setName(flashSaleDTO.getName());
		flashSale.setDescription(flashSaleDTO.getDescription());
		flashSale.setStartTime(flashSaleDTO.getStartTime());
		flashSale.setEndTime(flashSaleDTO.getEndTime());
		flashSale.setActive(flashSaleDTO.isActive());
		flashSale.setBannerImageUrl(flashSaleDTO.getBannerImageUrl());
		flashSale.setBackgroundColor(flashSaleDTO.getBackgroundColor());
		flashSale.setCreatedAt(new Date());
		flashSale.setUpdatedAt(new Date());

		FlashSale savedFlashSale = flashSaleRepository.save(flashSale);
		log.info("Flash sale created with id: {}", savedFlashSale.getId());

		return convertToDTO(savedFlashSale);
	}

	@Override
	public FlashSaleDTO updateFlashSale(FlashSaleDTO flashSaleDTO) {
		log.debug("Updating flash sale with id: {}", flashSaleDTO.getId());

		List<FlashSale> allFlashSales = (List<FlashSale>) flashSaleRepository.findAll();
		FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleDTO.getId())).findFirst()
				.orElseThrow(
						() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleDTO.getId()));

		flashSale.setName(flashSaleDTO.getName());
		flashSale.setDescription(flashSaleDTO.getDescription());
		flashSale.setStartTime(flashSaleDTO.getStartTime());
		flashSale.setEndTime(flashSaleDTO.getEndTime());
		flashSale.setActive(flashSaleDTO.isActive());
		flashSale.setBannerImageUrl(flashSaleDTO.getBannerImageUrl());
		flashSale.setBackgroundColor(flashSaleDTO.getBackgroundColor());
		flashSale.setUpdatedAt(new Date());

		FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);
		log.info("Flash sale updated with id: {}", updatedFlashSale.getId());

		return convertToDTO(updatedFlashSale);
	}

	@Override
	public void deleteFlashSale(Long flashSaleId) {
		log.debug("Deleting flash sale with id: {}", flashSaleId);

		List<FlashSale> flashSales = (List<FlashSale>) flashSaleRepository.findAll();
		FlashSale flashSale = flashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		// Delete associated products first
		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		products.forEach(flashSaleProductRepository::delete);

		// Then delete the flash sale
		flashSaleRepository.delete(flashSale);
		log.info("Flash sale deleted with id: {}", flashSaleId);
	}

	@Override
	public FlashSaleDTO getFlashSaleById(Long flashSaleId) {
		log.debug("Fetching flash sale with id: {}", flashSaleId);

		List<FlashSale> flashSales = (List<FlashSale>) flashSaleRepository.findAll();
		FlashSale flashSale = flashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		return convertToDTO(flashSale);
	}

	@Override
	public Page<FlashSaleDTO> getAllFlashSales(Pageable pageable) {
		log.debug("Fetching all flash sales with pagination");

		Page<FlashSale> flashSalesPage = flashSaleRepository.findAll(pageable);
		return flashSalesPage.map(this::convertToDTO);
	}

	@Override
	public List<FlashSaleDTO> getActiveFlashSales() {
		log.debug("Fetching active flash sales");

		List<FlashSale> flashSales = flashSaleRepository.findActive();
		return flashSales.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public List<FlashSaleDTO> getCurrentFlashSales() {
		log.debug("Fetching current flash sales");

		List<FlashSale> flashSales = flashSaleRepository.findCurrent();
		return flashSales.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public List<FlashSaleDTO> getUpcomingFlashSales() {
		log.debug("Fetching upcoming flash sales");

		List<FlashSale> flashSales = flashSaleRepository.findUpcoming();
		return flashSales.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public FlashSaleProductDTO addProductToFlashSale(Long flashSaleId, FlashSaleProductDTO productDTO) {
		log.debug("Adding product {} to flash sale {}", productDTO.getProductId(), flashSaleId);

		// Validate flash sale exists
		List<FlashSale> allFlashSales = (List<FlashSale>) flashSaleRepository.findAll();
		allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		// Validate product exists
		productRepository.findById(productDTO.getProductId()).orElseThrow(
				() -> new ResourceNotFoundException("Product not found with id: " + productDTO.getProductId()));

		// Check if product is already in this flash sale
		List<FlashSaleProduct> existingProducts = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		boolean productExists = existingProducts.stream()
				.anyMatch(fsp -> fsp.getProductId().equals(productDTO.getProductId()));

		if (productExists) {
			throw new BadRequestException("Product is already in this flash sale");
		}

		FlashSaleProduct flashSaleProduct = new FlashSaleProduct();
		flashSaleProduct.setFlashSaleId(flashSaleId);
		flashSaleProduct.setProductId(productDTO.getProductId());
		flashSaleProduct.setOriginalPrice(productDTO.getOriginalPrice());
		flashSaleProduct.setFlashPrice(productDTO.getFlashPrice());
		flashSaleProduct.setStockLimit(productDTO.getStockLimit());
		flashSaleProduct.setStockSold(0);
		flashSaleProduct.setMaxPerCustomer(productDTO.getMaxPerCustomer());
		flashSaleProduct.setDisplayOrder(productDTO.getDisplayOrder());
		flashSaleProduct.setActive(true);
		flashSaleProduct.setCreatedAt(new Date());

		FlashSaleProduct savedProduct = flashSaleProductRepository.save(flashSaleProduct);
		log.info("Product {} added to flash sale {}", productDTO.getProductId(), flashSaleId);

		return convertToDTO(savedProduct);
	}

	@Override
	public FlashSaleProductDTO updateFlashSaleProduct(FlashSaleProductDTO productDTO) {
		log.debug("Updating flash sale product with id: {}", productDTO.getId());

		List<FlashSaleProduct> allProducts = (List<FlashSaleProduct>) flashSaleProductRepository.findAll();
		FlashSaleProduct flashSaleProduct = allProducts.stream().filter(fsp -> fsp.getId().equals(productDTO.getId()))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException(
						"Flash sale product not found with id: " + productDTO.getId()));

		flashSaleProduct.setOriginalPrice(productDTO.getOriginalPrice());
		flashSaleProduct.setFlashPrice(productDTO.getFlashPrice());
		flashSaleProduct.setStockLimit(productDTO.getStockLimit());
		flashSaleProduct.setMaxPerCustomer(productDTO.getMaxPerCustomer());
		flashSaleProduct.setDisplayOrder(productDTO.getDisplayOrder());
		flashSaleProduct.setActive(productDTO.isActive());

		FlashSaleProduct updatedProduct = flashSaleProductRepository.save(flashSaleProduct);
		log.info("Flash sale product updated with id: {}", updatedProduct.getId());

		return convertToDTO(updatedProduct);
	}

	@Override
	public void removeProductFromFlashSale(Long flashSaleId, Long productId) {
		log.debug("Removing product {} from flash sale {}", productId, flashSaleId);

		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		FlashSaleProduct flashSaleProduct = products.stream().filter(fsp -> fsp.getProductId().equals(productId))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException("Product not found in flash sale"));

		flashSaleProductRepository.delete(flashSaleProduct);
		log.info("Product {} removed from flash sale {}", productId, flashSaleId);
	}

	@Override
	public List<FlashSaleProductDTO> getFlashSaleProducts(Long flashSaleId) {
		log.debug("Fetching products for flash sale: {}", flashSaleId);

		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		return products.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public Page<FlashSaleProductDTO> getFlashSaleProducts(Long flashSaleId, Pageable pageable) {
		log.debug("Fetching paginated products for flash sale: {}", flashSaleId);

		// Since repository doesn't support pagination, use findAll + manual pagination
		List<FlashSaleProduct> allProducts = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		List<FlashSaleProductDTO> productDTOs = allProducts.stream().map(this::convertToDTO)
				.collect(Collectors.toList());

		return new PageImpl<>(productDTOs, pageable, productDTOs.size());
	}

	@Override
	public FlashSaleDTO getCurrentActiveFlashSale() {
		log.debug("Fetching current active flash sale");

		Date now = new Date();
		List<FlashSale> activeFlashSales = flashSaleRepository.findActive();

		// Filter for currently active flash sales
		FlashSale currentFlashSale = activeFlashSales.stream()
				.filter(fs -> fs.getStartTime().before(now) && fs.getEndTime().after(now)).findFirst().orElse(null);

		if (currentFlashSale == null) {
			return null;
		}

		return convertToDTO(currentFlashSale);
	}

	@Override
	public List<FlashSaleProductDTO> getCurrentFlashSaleProducts() {
		log.debug("Fetching current flash sale products");

		FlashSaleDTO currentFlashSale = getCurrentActiveFlashSale();
		if (currentFlashSale == null) {
			return List.of();
		}

		return getFlashSaleProducts(currentFlashSale.getId());
	}

	@Override
	public FlashSaleProductDTO getFlashSaleProduct(Long flashSaleId, Long productId) {
		log.debug("Fetching flash sale product: {} from flash sale: {}", productId, flashSaleId);

		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		FlashSaleProduct flashSaleProduct = products.stream().filter(fsp -> fsp.getProductId().equals(productId))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException("Product not found in flash sale"));

		return convertToDTO(flashSaleProduct);
	}

	@Override
	public boolean canPurchaseFlashSaleProduct(Long flashSaleId, Long productId, int quantity, Long userId) {
		log.debug("Checking if user {} can purchase {} units of product {} from flash sale {}", userId, quantity,
				productId, flashSaleId);

		try {
			List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
			FlashSaleProduct flashSaleProduct = products.stream().filter(fsp -> fsp.getProductId().equals(productId))
					.findFirst().orElse(null);

			if (flashSaleProduct == null || !flashSaleProduct.isActive()) {
				return false;
			}

			// Check flash sale is active
			List<FlashSale> allFlashSales = (List<FlashSale>) flashSaleRepository.findAll();
			FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
					.orElse(null);

			if (flashSale == null || !flashSale.isCurrentlyActive()) {
				return false;
			}

			return flashSaleProduct.canPurchase(quantity);
		} catch (Exception e) {
			log.error("Error checking purchase eligibility", e);
			return false;
		}
	}

	@Override
	public void processFlashSalePurchase(Long flashSaleId, Long productId, int quantity, Long userId) {
		log.debug("Processing flash sale purchase: {} units of product {} for user {}", quantity, productId, userId);

		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		FlashSaleProduct flashSaleProduct = products.stream().filter(fsp -> fsp.getProductId().equals(productId))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException("Product not found in flash sale"));

		if (!canPurchaseFlashSaleProduct(flashSaleId, productId, quantity, userId)) {
			throw new BadRequestException("Cannot purchase this flash sale product");
		}

		// Update stock sold
		int currentSold = flashSaleProduct.getStockSold() != null ? flashSaleProduct.getStockSold() : 0;
		flashSaleProduct.setStockSold(currentSold + quantity);

		flashSaleProductRepository.save(flashSaleProduct);
		log.info("Flash sale purchase processed: {} units of product {} for user {}", quantity, productId, userId);
	}

	@Override
	public FlashSaleDTO activateFlashSale(Long flashSaleId) {
		log.debug("Activating flash sale: {}", flashSaleId);

		List<FlashSale> allFlashSales = (List<FlashSale>) flashSaleRepository.findAll();
		FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		flashSale.setActive(true);
		flashSale.setUpdatedAt(new Date());

		FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);
		log.info("Flash sale activated: {}", flashSaleId);

		return convertToDTO(updatedFlashSale);
	}

	@Override
	public FlashSaleDTO deactivateFlashSale(Long flashSaleId) {
		log.debug("Deactivating flash sale: {}", flashSaleId);

		List<FlashSale> allFlashSales = (List<FlashSale>) flashSaleRepository.findAll();
		FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		flashSale.setActive(false);
		flashSale.setUpdatedAt(new Date());

		FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);
		log.info("Flash sale deactivated: {}", flashSaleId);

		return convertToDTO(updatedFlashSale);
	}

	@Override
	public Long getTotalSalesForFlashSale(Long flashSaleId) {
		log.debug("Getting total sales for flash sale: {}", flashSaleId);

		// Fallback implementation - calculate from flash sale products
		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		return products.stream().mapToLong(fsp -> fsp.getStockSold() != null ? fsp.getStockSold() : 0).sum();
	}

	@Override
	public BigDecimal getTotalRevenueForFlashSale(Long flashSaleId) {
		log.debug("Getting total revenue for flash sale: {}", flashSaleId);

		// Fallback implementation - calculate from flash sale products
		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		return products.stream().map(fsp -> {
			int sold = fsp.getStockSold() != null ? fsp.getStockSold() : 0;
			BigDecimal price = fsp.getFlashPrice() != null ? fsp.getFlashPrice() : BigDecimal.ZERO;
			return price.multiply(BigDecimal.valueOf(sold));
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private FlashSaleDTO convertToDTO(FlashSale flashSale) {
		FlashSaleDTO dto = new FlashSaleDTO();
		dto.setId(flashSale.getId());
		dto.setName(flashSale.getName());
		dto.setDescription(flashSale.getDescription());
		dto.setStartTime(flashSale.getStartTime());
		dto.setEndTime(flashSale.getEndTime());
		dto.setActive(flashSale.isActive());
		dto.setBannerImageUrl(flashSale.getBannerImageUrl());
		dto.setBackgroundColor(flashSale.getBackgroundColor());
		dto.setCreatedAt(flashSale.getCreatedAt());
		dto.setUpdatedAt(flashSale.getUpdatedAt());

		// Set computed fields
		dto.setCurrentlyActive(flashSale.isCurrentlyActive());
		dto.setUpcoming(flashSale.isUpcoming());
		dto.setExpired(flashSale.isExpired());
		dto.setRemainingTimeInMinutes(flashSale.getRemainingTimeInMinutes());

		// Set statistics
		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSale.getId());
		dto.setTotalProducts(products.size());
		dto.setTotalSales(getTotalSalesForFlashSale(flashSale.getId()));
		dto.setTotalRevenue(getTotalRevenueForFlashSale(flashSale.getId()));

		return dto;
	}

	private FlashSaleProductDTO convertToDTO(FlashSaleProduct flashSaleProduct) {
		FlashSaleProductDTO dto = new FlashSaleProductDTO();
		dto.setId(flashSaleProduct.getId());
		dto.setFlashSaleId(flashSaleProduct.getFlashSaleId());
		dto.setProductId(flashSaleProduct.getProductId());
		dto.setOriginalPrice(flashSaleProduct.getOriginalPrice());
		dto.setFlashPrice(flashSaleProduct.getFlashPrice());
		dto.setStockLimit(flashSaleProduct.getStockLimit());
		dto.setStockSold(flashSaleProduct.getStockSold());
		dto.setMaxPerCustomer(flashSaleProduct.getMaxPerCustomer());
		dto.setDisplayOrder(flashSaleProduct.getDisplayOrder());
		dto.setActive(flashSaleProduct.isActive());
		dto.setCreatedAt(flashSaleProduct.getCreatedAt());

		// Set computed fields
		dto.setDiscountAmount(flashSaleProduct.getDiscountAmount());
		dto.setDiscountPercentage(flashSaleProduct.getDiscountPercentage());
		dto.setRemainingStock(flashSaleProduct.getRemainingStock());
		dto.setSoldOut(flashSaleProduct.isSoldOut());

		// Set product information
		Product product = productRepository.findById(flashSaleProduct.getProductId()).orElse(null);
		if (product != null) {
			dto.setProductName(product.getName());
			dto.setProductImageUrl(product.getImageUrl());
			dto.setProductSku(product.getSku());
			dto.setProductDescription(product.getDescription());
			dto.setBrandName(product.getBrandName());
			dto.setCategoryName(product.getCategoryName());
		}

		// Set flash sale information
		List<FlashSale> allFlashSales = (List<FlashSale>) flashSaleRepository.findAll();
		FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleProduct.getFlashSaleId()))
				.findFirst().orElse(null);
		if (flashSale != null) {
			dto.setFlashSaleName(flashSale.getName());
		}

		return dto;
	}
}
