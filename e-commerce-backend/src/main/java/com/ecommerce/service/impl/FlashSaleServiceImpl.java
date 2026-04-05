package com.ecommerce.service.impl;

import com.ecommerce.dto.FlashSaleDTO;
import com.ecommerce.dto.FlashSaleProductDTO;
import com.ecommerce.dto.NotificationDTO;
import com.ecommerce.entity.FlashSale;
import com.ecommerce.entity.FlashSaleProduct;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductImage;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.FlashSaleRepository;
import com.ecommerce.repository.FlashSaleProductRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.FlashSaleService;
import com.ecommerce.service.NotificationService;
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
/**
 * author: LeTuBac
 */
public class FlashSaleServiceImpl implements FlashSaleService {

	private final FlashSaleRepository flashSaleRepository;
	private final FlashSaleProductRepository flashSaleProductRepository;
	private final ProductRepository productRepository;
	private final NotificationService notificationService;

	@Override
	public FlashSaleDTO createFlashSale(FlashSaleDTO flashSaleDTO) {
		log.debug("Creating flash sale: {}", flashSaleDTO.getName());

		// Validate: no overlapping flash sales
		List<FlashSale> overlapping = flashSaleRepository.findOverlapping(
				flashSaleDTO.getStartTime(), flashSaleDTO.getEndTime(), null);
		if (!overlapping.isEmpty()) {
			FlashSale conflict = overlapping.get(0);
			throw new BadRequestException("Thời gian chồng chéo với Flash Sale \"" + conflict.getName()
					+ "\" (" + conflict.getStartTime() + " – " + conflict.getEndTime() + ")");
		}

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

		Long generatedId = flashSaleRepository.insertFlashSale(
				flashSale.getName(), flashSale.getDescription(),
				flashSale.getStartTime(), flashSale.getEndTime(),
				flashSale.isActive(), flashSale.getBannerImageUrl(),
				flashSale.getBackgroundColor(), flashSale.getCreatedAt(), flashSale.getUpdatedAt());
		flashSale.setId(generatedId);
		log.info("Flash sale created with id: {}", generatedId);

		return convertToDTO(flashSale);
	}

	@Override
	public FlashSaleDTO updateFlashSale(FlashSaleDTO flashSaleDTO) {
		log.debug("Updating flash sale with id: {}", flashSaleDTO.getId());

		List<FlashSale> allFlashSales = flashSaleRepository.findAllFlashSales();
		FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleDTO.getId())).findFirst()
				.orElseThrow(
						() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleDTO.getId()));

		// Lock check: cannot edit if flash sale has products
		List<FlashSaleProduct> existingProds = flashSaleProductRepository.findByFlashSaleId(flashSaleDTO.getId());
		if (!existingProds.isEmpty()) {
			throw new BadRequestException(
					"Không thể chỉnh sửa Flash Sale đang có sản phẩm. Vui lòng xóa tất cả sản phẩm trước.");
		}

		// Overlap check (exclude self)
		List<FlashSale> overlapping = flashSaleRepository.findOverlapping(
				flashSaleDTO.getStartTime(), flashSaleDTO.getEndTime(), flashSaleDTO.getId());
		if (!overlapping.isEmpty()) {
			FlashSale conflict = overlapping.get(0);
			throw new BadRequestException("Thời gian chồng chéo với Flash Sale \"" + conflict.getName() + "\"");
		}

		flashSale.setName(flashSaleDTO.getName());
		flashSale.setDescription(flashSaleDTO.getDescription());
		flashSale.setStartTime(flashSaleDTO.getStartTime());
		flashSale.setEndTime(flashSaleDTO.getEndTime());
		flashSale.setActive(flashSaleDTO.isActive());
		flashSale.setBannerImageUrl(flashSaleDTO.getBannerImageUrl());
		flashSale.setBackgroundColor(flashSaleDTO.getBackgroundColor());
		flashSale.setUpdatedAt(new Date());

		flashSaleRepository.updateFlashSale(
				flashSale.getId(), flashSale.getName(), flashSale.getDescription(),
				flashSale.getStartTime(), flashSale.getEndTime(),
				flashSale.isActive(), flashSale.getBannerImageUrl(),
				flashSale.getBackgroundColor(), flashSale.getUpdatedAt());
		log.info("Flash sale updated with id: {}", flashSale.getId());

		return convertToDTO(flashSale);
	}

	@Override
	public void deleteFlashSale(Long flashSaleId) {
		log.debug("Deleting flash sale with id: {}", flashSaleId);

		List<FlashSale> flashSales = flashSaleRepository.findAllFlashSales();
		FlashSale flashSale = flashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		// Lock check: cannot delete if has products
		List<FlashSaleProduct> products = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		if (!products.isEmpty()) {
			throw new BadRequestException(
					"Không thể xóa Flash Sale đang có sản phẩm. Vui lòng xóa tất cả sản phẩm trước.");
		}

		flashSaleRepository.delete(flashSale);
		log.info("Flash sale deleted with id: {}", flashSaleId);
	}

	@Override
	public FlashSaleDTO getFlashSaleById(Long flashSaleId) {
		log.debug("Fetching flash sale with id: {}", flashSaleId);

		List<FlashSale> flashSales = flashSaleRepository.findAllFlashSales();
		FlashSale flashSale = flashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		return convertToDTO(flashSale);
	}

	@Override
	public Page<FlashSaleDTO> getAllFlashSales(Pageable pageable) {
		log.debug("Fetching all flash sales with pagination");

		List<FlashSale> allFlashSales = flashSaleRepository.findAllFlashSales();
		log.debug("[getAllFlashSales] findAllFlashSales() returned {} records", allFlashSales.size());
		List<FlashSaleDTO> dtos = allFlashSales.stream().map(this::convertToDTO).collect(Collectors.toList());

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), dtos.size());
		return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
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

		// Validate flash sale exists and can accept new products
		List<FlashSale> allFlashSales = flashSaleRepository.findAllFlashSales();
		FlashSale targetFlashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		if (!targetFlashSale.isActive()) {
			throw new BadRequestException("Không thể thêm sản phẩm vào Flash Sale đang tắt (inactive)");
		}
		if (targetFlashSale.isExpired()) {
			throw new BadRequestException("Không thể thêm sản phẩm vào Flash Sale đã hết hạn");
		}

		// Validate product exists and is not out-of-stock
		Product product = productRepository.findById(productDTO.getProductId()).orElseThrow(
				() -> new ResourceNotFoundException("Product not found with id: " + productDTO.getProductId()));
		if (!product.isActive()) {
			throw new BadRequestException("Sản phẩm đang bị tắt, không thể thêm vào Flash Sale");
		}
		if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
			throw new BadRequestException(
					"Sản phẩm \"" + product.getName() + "\" đã hết hàng, không thể thêm vào Flash Sale");
		}

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

		Long generatedProductId = flashSaleProductRepository.insertFlashSaleProduct(
				flashSaleProduct.getFlashSaleId(), flashSaleProduct.getProductId(),
				flashSaleProduct.getOriginalPrice(), flashSaleProduct.getFlashPrice(),
				flashSaleProduct.getStockLimit(), flashSaleProduct.getStockSold(),
				flashSaleProduct.getMaxPerCustomer(), flashSaleProduct.getDisplayOrder(),
				flashSaleProduct.isActive(), flashSaleProduct.getCreatedAt());
		flashSaleProduct.setId(generatedProductId);
		log.info("Product {} added to flash sale {}", productDTO.getProductId(), flashSaleId);

		return convertToDTO(flashSaleProduct);
	}

	@Override
	public FlashSaleProductDTO updateFlashSaleProduct(FlashSaleProductDTO productDTO) {
		log.debug("Updating flash sale product with id: {}", productDTO.getId());

		List<FlashSaleProduct> allProducts = flashSaleProductRepository.findByFlashSaleId(productDTO.getFlashSaleId());
		FlashSaleProduct flashSaleProduct = allProducts.stream()
				.filter(fsp -> fsp.getProductId().equals(productDTO.getProductId()))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException(
						"Flash sale product not found for productId: " + productDTO.getProductId()));

		flashSaleProduct.setOriginalPrice(productDTO.getOriginalPrice());
		flashSaleProduct.setFlashPrice(productDTO.getFlashPrice());
		flashSaleProduct.setStockLimit(productDTO.getStockLimit());
		flashSaleProduct.setMaxPerCustomer(productDTO.getMaxPerCustomer());
		flashSaleProduct.setDisplayOrder(productDTO.getDisplayOrder());
		flashSaleProduct.setActive(productDTO.isActive());

		flashSaleProductRepository.updateFlashSaleProduct(
				flashSaleProduct.getId(), flashSaleProduct.getOriginalPrice(),
				flashSaleProduct.getFlashPrice(), flashSaleProduct.getStockLimit(),
				flashSaleProduct.getMaxPerCustomer(), flashSaleProduct.getDisplayOrder(),
				flashSaleProduct.isActive());
		log.info("Flash sale product updated with id: {}", flashSaleProduct.getId());

		return convertToDTO(flashSaleProduct);
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

		// Only return if there are active products
		List<FlashSaleProduct> activeProducts = flashSaleProductRepository
				.findActiveByFlashSaleId(currentFlashSale.getId());
		if (activeProducts.isEmpty()) {
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
			List<FlashSale> allFlashSales = flashSaleRepository.findAllFlashSales();
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

		flashSaleProductRepository.updateSoldQuantity(flashSaleProduct.getId(), flashSaleProduct.getStockSold());
		log.info("Flash sale purchase processed: {} units of product {} for user {}", quantity, productId, userId);
	}

	@Override
	public FlashSaleDTO activateFlashSale(Long flashSaleId) {
		log.debug("Activating flash sale: {}", flashSaleId);

		FlashSale flashSale = flashSaleRepository.findAllFlashSales().stream()
				.filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		// Validate: flash sale must not be expired
		if (flashSale.isExpired()) {
			throw new BadRequestException("Không thể kích hoạt Flash Sale đã hết hạn (endTime đã qua).");
		}
		// Validate: must have at least one active product
		List<FlashSaleProduct> activeProds = flashSaleProductRepository.findActiveByFlashSaleId(flashSaleId);
		if (activeProds.isEmpty()) {
			throw new BadRequestException(
					"Không thể kích hoạt Flash Sale khi chưa có sản phẩm nào. Hãy thêm sản phẩm trước.");
		}

		flashSaleRepository.updateStatus(flashSaleId, true);
		log.info("Flash sale activated: {}", flashSaleId);

		return getFlashSaleById(flashSaleId);
	}

	@Override
	public FlashSaleDTO deactivateFlashSale(Long flashSaleId) {
		log.debug("Deactivating flash sale: {}", flashSaleId);

		FlashSale flashSale = flashSaleRepository.findAllFlashSales().stream()
				.filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		if (!flashSale.isActive()) {
			throw new BadRequestException("Flash Sale này đang ở trạng thái tắt rồi.");
		}

		flashSaleRepository.updateStatus(flashSaleId, false);
		log.info("Flash sale deactivated: {}", flashSaleId);

		return getFlashSaleById(flashSaleId);
	}

	@Override
	public FlashSaleDTO cloneFlashSale(Long flashSaleId, FlashSaleDTO overrides) {
		log.debug("Cloning flash sale: {}", flashSaleId);

		List<FlashSale> allFlashSales = flashSaleRepository.findAllFlashSales();
		FlashSale source = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + flashSaleId));

		// Apply overrides for time, name. Others fall back to source values.
		Date newStart = overrides != null && overrides.getStartTime() != null ? overrides.getStartTime()
				: source.getStartTime();
		Date newEnd = overrides != null && overrides.getEndTime() != null ? overrides.getEndTime()
				: source.getEndTime();
		String newName = overrides != null && overrides.getName() != null && !overrides.getName().isBlank()
				? overrides.getName()
				: "Copy of " + source.getName();

		// Validate no time overlap
		List<FlashSale> overlapping = flashSaleRepository.findOverlapping(newStart, newEnd, null);
		if (!overlapping.isEmpty()) {
			FlashSale conflict = overlapping.get(0);
			throw new BadRequestException("Thời gian chồng chéo với Flash Sale \"" + conflict.getName()
					+ "\" (" + conflict.getStartTime() + " – " + conflict.getEndTime() + ")");
		}

		// Create the cloned flash sale (always inactive)
		Date now = new Date();
		Long newId = flashSaleRepository.insertFlashSale(
				newName,
				source.getDescription(),
				newStart, newEnd,
				false,
				source.getBannerImageUrl(),
				source.getBackgroundColor(),
				now, now);

		// Copy all products with stockSold reset to 0
		List<FlashSaleProduct> sourceProducts = flashSaleProductRepository.findByFlashSaleId(flashSaleId);
		for (FlashSaleProduct sp : sourceProducts) {
			flashSaleProductRepository.insertFlashSaleProduct(
					newId, sp.getProductId(),
					sp.getOriginalPrice(), sp.getFlashPrice(),
					sp.getStockLimit(), 0,
					sp.getMaxPerCustomer(), sp.getDisplayOrder(),
					false, now);
		}

		log.info("Cloned flash sale {} → new id={}", flashSaleId, newId);
		return getFlashSaleById(newId);
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

	@Override
	public void syncFlashSaleStatus() {
		// 1. Auto-activate flash sales that should now be running
		List<FlashSale> toActivate = flashSaleRepository.findToActivate();
		if (!toActivate.isEmpty()) {
			for (FlashSale fs : toActivate) {
				// Only activate if it has at least one active product
				List<FlashSaleProduct> activeProds = flashSaleProductRepository.findActiveByFlashSaleId(fs.getId());
				if (!activeProds.isEmpty()) {
					flashSaleRepository.updateStatus(fs.getId(), true);
					log.info("[Scheduler] Auto-activated flash sale id={} name={}", fs.getId(), fs.getName());
					// Notify all users
					try {
						NotificationDTO notif = new NotificationDTO();
						notif.setTitle("🔥 Flash Sale bắt đầu!");
						notif.setMessage(fs.getName() + " đã bắt đầu! Đừng bỏ lỡ cơ hội mà.");
						notif.setType("FLASH_SALE");
						notif.setEntityType("FLASH_SALE");
						notif.setEntityId(fs.getId());
						notif.setPriority("HIGH");
						notif.setLink("/flash-sale");
						notificationService.broadcastToAll(notif);
					} catch (Exception e) {
						log.warn("[Scheduler] Failed to send activate notification for flash sale id={}: {}", fs.getId(),
								e.getMessage());
					}
				}
			}
		}

		// 2. Auto-expire flash sales that have passed their end time
		List<FlashSale> expired = flashSaleRepository.findExpiredActive();
		if (!expired.isEmpty()) {
			for (FlashSale fs : expired) {
				flashSaleRepository.updateStatus(fs.getId(), false);
				log.info("[Scheduler] Auto-expired flash sale id={} name={}", fs.getId(), fs.getName());
				// Notify admins that flash sale has ended
				try {
					NotificationDTO notif = new NotificationDTO();
					notif.setTitle("⏱️ Flash Sale kết thúc");
					notif.setMessage(fs.getName() + " đã kết thúc.");
					notif.setType("FLASH_SALE");
					notif.setEntityType("FLASH_SALE");
					notif.setEntityId(fs.getId());
					notif.setPriority("NORMAL");
					notif.setTargetRole("ADMIN");
					notificationService.broadcastToRole("ADMIN", notif);
				} catch (Exception e) {
					log.warn("[Scheduler] Failed to send expire notification for flash sale id={}: {}", fs.getId(),
							e.getMessage());
				}
			}
		}

		// 3. Mark sold-out products as inactive for all currently active flash sales
		List<FlashSale> activeFlashSales = flashSaleRepository.findActive();
		if (!activeFlashSales.isEmpty()) {
			for (FlashSale fs : activeFlashSales) {
				List<FlashSaleProduct> soldOut = flashSaleProductRepository.findSoldOutByFlashSaleId(fs.getId());
				for (FlashSaleProduct fsp : soldOut) {
					flashSaleProductRepository.updateActiveStatus(fsp.getId(), false);
					log.info("[Scheduler] Marked sold-out product id={} in flash sale id={}", fsp.getProductId(),
							fs.getId());
				}
			}
		}
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
		dto.setHasProducts(!products.isEmpty());
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
			List<ProductImage> productImages = productRepository.findImagesByProductId(product.getId());
			String imgUrl = productImages.stream()
					.filter(ProductImage::isPrimary)
					.map(ProductImage::getImageUrl)
					.findFirst()
					.orElse(productImages.isEmpty() ? null : productImages.get(0).getImageUrl());
			dto.setProductImageUrl(imgUrl);
			dto.setProductSku(product.getSku());
			dto.setProductDescription(product.getDescription());
			dto.setBrandName(product.getBrandName());
			dto.setCategoryName(product.getCategoryName());
		}

		// Set flash sale information
		List<FlashSale> allFlashSales = flashSaleRepository.findAllFlashSales();
		FlashSale flashSale = allFlashSales.stream().filter(fs -> fs.getId().equals(flashSaleProduct.getFlashSaleId()))
				.findFirst().orElse(null);
		if (flashSale != null) {
			dto.setFlashSaleName(flashSale.getName());
		}

		return dto;
	}
}
