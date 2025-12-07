package com.ecommerce.service;

import com.ecommerce.dto.FlashSaleDTO;
import com.ecommerce.dto.FlashSaleProductDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;

public interface FlashSaleService {

    // Flash Sale management
    FlashSaleDTO createFlashSale(FlashSaleDTO flashSaleDTO) throws DetailException;

    FlashSaleDTO updateFlashSale(FlashSaleDTO flashSaleDTO) throws DetailException;

    void deleteFlashSale(Long flashSaleId) throws DetailException;

    FlashSaleDTO getFlashSaleById(Long flashSaleId) throws DetailException;

    Page<FlashSaleDTO> getAllFlashSales(Pageable pageable) throws DetailException;

    List<FlashSaleDTO> getActiveFlashSales() throws DetailException;

    List<FlashSaleDTO> getCurrentFlashSales() throws DetailException;

    List<FlashSaleDTO> getUpcomingFlashSales() throws DetailException;

    // Flash Sale Products management
    FlashSaleProductDTO addProductToFlashSale(Long flashSaleId, FlashSaleProductDTO productDTO) throws DetailException;

    FlashSaleProductDTO updateFlashSaleProduct(FlashSaleProductDTO productDTO) throws DetailException;

    void removeProductFromFlashSale(Long flashSaleId, Long productId) throws DetailException;

    List<FlashSaleProductDTO> getFlashSaleProducts(Long flashSaleId) throws DetailException;

    Page<FlashSaleProductDTO> getFlashSaleProducts(Long flashSaleId, Pageable pageable) throws DetailException;

    // Customer facing methods
    FlashSaleDTO getCurrentActiveFlashSale() throws DetailException;

    List<FlashSaleProductDTO> getCurrentFlashSaleProducts() throws DetailException;

    FlashSaleProductDTO getFlashSaleProduct(Long flashSaleId, Long productId) throws DetailException;

    // Business operations
    boolean canPurchaseFlashSaleProduct(Long flashSaleId, Long productId, int quantity, Long userId)
            throws DetailException;

    void processFlashSalePurchase(Long flashSaleId, Long productId, int quantity, Long userId) throws DetailException;

    // Admin operations
    FlashSaleDTO activateFlashSale(Long flashSaleId) throws DetailException;

    FlashSaleDTO deactivateFlashSale(Long flashSaleId) throws DetailException;

    // Statistics
    Long getTotalSalesForFlashSale(Long flashSaleId) throws DetailException;

    java.math.BigDecimal getTotalRevenueForFlashSale(Long flashSaleId) throws DetailException;
}