package com.ecommerce.service;

import com.ecommerce.dto.FlashSaleDTO;
import com.ecommerce.dto.FlashSaleProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;

public interface FlashSaleService {

    // Flash Sale management
    FlashSaleDTO createFlashSale(FlashSaleDTO flashSaleDTO);

    FlashSaleDTO updateFlashSale(FlashSaleDTO flashSaleDTO);

    void deleteFlashSale(Long flashSaleId);

    FlashSaleDTO getFlashSaleById(Long flashSaleId);

    Page<FlashSaleDTO> getAllFlashSales(Pageable pageable);

    List<FlashSaleDTO> getActiveFlashSales();

    List<FlashSaleDTO> getCurrentFlashSales();

    List<FlashSaleDTO> getUpcomingFlashSales();

    // Flash Sale Products management
    FlashSaleProductDTO addProductToFlashSale(Long flashSaleId, FlashSaleProductDTO productDTO);

    FlashSaleProductDTO updateFlashSaleProduct(FlashSaleProductDTO productDTO);

    void removeProductFromFlashSale(Long flashSaleId, Long productId);

    List<FlashSaleProductDTO> getFlashSaleProducts(Long flashSaleId);

    Page<FlashSaleProductDTO> getFlashSaleProducts(Long flashSaleId, Pageable pageable);

    // Customer facing methods
    FlashSaleDTO getCurrentActiveFlashSale();

    List<FlashSaleProductDTO> getCurrentFlashSaleProducts();

    FlashSaleProductDTO getFlashSaleProduct(Long flashSaleId, Long productId);

    // Business operations
    boolean canPurchaseFlashSaleProduct(Long flashSaleId, Long productId, int quantity, Long userId);

    void processFlashSalePurchase(Long flashSaleId, Long productId, int quantity, Long userId);

    // Admin operations
    FlashSaleDTO activateFlashSale(Long flashSaleId);

    FlashSaleDTO deactivateFlashSale(Long flashSaleId);

    // Statistics
    Long getTotalSalesForFlashSale(Long flashSaleId);

    java.math.BigDecimal getTotalRevenueForFlashSale(Long flashSaleId);
}