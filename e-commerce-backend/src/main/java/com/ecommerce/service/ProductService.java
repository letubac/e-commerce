package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    // Product management
    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    Optional<ProductDTO> getProductById(Long id);

    ProductDTO getProductByIdOrThrow(Long id);

    Optional<ProductDTO> getProductBySlug(String slug);

    Optional<ProductDTO> getProductBySku(String sku);

    List<ProductDTO> getAllProducts();

    Page<ProductDTO> getAllProducts(Pageable pageable);

    List<ProductDTO> getActiveProducts();

    List<ProductDTO> getFeaturedProducts();

    void deleteProduct(Long id);

    void deactivateProduct(Long id);

    void activateProduct(Long id);

    // Product filtering and search
    List<ProductDTO> getProductsByCategory(Long categoryId);

    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    List<ProductDTO> getProductsByBrand(Long brandId);

    Page<ProductDTO> getProductsByBrand(Long brandId, Pageable pageable);

    List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductDTO> searchProducts(String keyword);

    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);

    List<ProductDTO> getLowStockProducts();

    // Pagination
    List<ProductDTO> getProductsWithPagination(int page, int size);

    List<ProductDTO> getActiveProductsPaginated(int page, int size);

    List<ProductDTO> getProductsByCategoryPaginated(Long categoryId, int page, int size);

    // Stock management
    void updateStock(Long productId, Integer newStock);

    void increaseStock(Long productId, Integer quantity);

    void decreaseStock(Long productId, Integer quantity);

    boolean isInStock(Long productId);

    // Price management
    void updatePrice(Long productId, BigDecimal newPrice);

    void setSalePrice(Long productId, BigDecimal salePrice);

    void removeSalePrice(Long productId);

    // Validation
    boolean isSkuAvailable(String sku);

    boolean isSlugAvailable(String slug);

    // Statistics
    long getTotalProductCount();

    long getActiveProductCount();

    long getProductCountByCategory(Long categoryId);

    long getProductCountByBrand(Long brandId);

    // Additional methods for controller compatibility
    Page<ProductDTO> findByCategoryId(Long categoryId, Pageable pageable,
            Double minPrice, Double maxPrice,
            Long brandId, Boolean active);

    Page<ProductDTO> findByBrandId(Long brandId, Pageable pageable,
            Double minPrice, Double maxPrice,
            Long categoryId, Boolean active);
}