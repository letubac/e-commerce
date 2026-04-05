package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * author: LeTuBac
 */
public interface ProductService {

        // Product management
        ProductDTO createProduct(ProductDTO productDTO) throws DetailException;

        ProductDTO updateProduct(Long id, ProductDTO productDTO) throws DetailException;

        Optional<ProductDTO> getProductById(Long id) throws DetailException;

        ProductDTO getProductByIdOrThrow(Long id) throws DetailException;

        Optional<ProductDTO> getProductBySlug(String slug) throws DetailException;

        Optional<ProductDTO> getProductBySku(String sku) throws DetailException;

        List<ProductDTO> getAllProducts() throws DetailException;

        Page<ProductDTO> getAllProducts(Pageable pageable) throws DetailException;

        List<ProductDTO> getActiveProducts() throws DetailException;

        List<ProductDTO> getFeaturedProducts() throws DetailException;

        void deleteProduct(Long id) throws DetailException;

        ProductDTO deactivateProduct(Long id) throws DetailException;

        ProductDTO activateProduct(Long id) throws DetailException;

        // Product filtering and search
        List<ProductDTO> getProductsByCategory(Long categoryId) throws DetailException;

        Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) throws DetailException;

        List<ProductDTO> getProductsByBrand(Long brandId) throws DetailException;

        Page<ProductDTO> getProductsByBrand(Long brandId, Pageable pageable) throws DetailException;

        List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) throws DetailException;

        List<ProductDTO> searchProducts(String keyword) throws DetailException;

        Page<ProductDTO> searchProducts(String keyword, Pageable pageable) throws DetailException;

        List<ProductDTO> getLowStockProducts() throws DetailException;

        // Pagination
        List<ProductDTO> getProductsWithPagination(int page, int size) throws DetailException;

        List<ProductDTO> getActiveProductsPaginated(int page, int size) throws DetailException;

        List<ProductDTO> getProductsByCategoryPaginated(Long categoryId, int page, int size) throws DetailException;

        // Stock management
        ProductDTO updateStock(Long productId, Integer newStock) throws DetailException;

        void increaseStock(Long productId, Integer quantity) throws DetailException;

        void decreaseStock(Long productId, Integer quantity) throws DetailException;

        boolean isInStock(Long productId) throws DetailException;

        // Price management
        void updatePrice(Long productId, BigDecimal newPrice) throws DetailException;

        void setSalePrice(Long productId, BigDecimal salePrice) throws DetailException;

        void removeSalePrice(Long productId) throws DetailException;

        // Validation
        boolean isSkuAvailable(String sku) throws DetailException;

        boolean isSlugAvailable(String slug) throws DetailException;

        // Statistics
        long getTotalProductCount() throws DetailException;

        long getActiveProductCount() throws DetailException;

        long getProductCountByCategory(Long categoryId) throws DetailException;

        long getProductCountByBrand(Long brandId) throws DetailException;

        // Additional methods for controller compatibility
        Page<ProductDTO> findByCategoryId(Long categoryId, Pageable pageable,
                        Double minPrice, Double maxPrice,
                        Long brandId, Boolean active) throws DetailException;

        Page<ProductDTO> findByBrandId(Long brandId, Pageable pageable,
                        Double minPrice, Double maxPrice,
                        Long categoryId, Boolean active) throws DetailException;
}