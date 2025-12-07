package com.ecommerce.controller;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.ProductService;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing products.
 * Provides endpoints for product CRUD operations and search functionality.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get products with filtering and pagination
     */
    @GetMapping("/products")
    public ResponseEntity<BusinessApiResponse> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchProducts(keyword, pageRequest);
            } else if (categoryId != null) {
                products = productService.getProductsByCategory(categoryId, pageRequest);
            } else if (brandId != null && brandId != 0) {
                products = productService.getProductsByBrand(brandId, pageRequest);
            } else {
                products = productService.getAllProducts(pageRequest);
            }

            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<BusinessApiResponse> getProduct(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        try {
            ProductDTO product = productService.getProductByIdOrThrow(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(product, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Search products
     */
    @GetMapping("/products/search")
    public ResponseEntity<BusinessApiResponse> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<ProductDTO> products = productService.searchProducts(keyword, pageRequest);
            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get search suggestions
     */
    @GetMapping("/products/search/suggestions")
    public ResponseEntity<BusinessApiResponse> getSearchSuggestions(@RequestParam String keyword) {
        long start = System.currentTimeMillis();
        try {
            List<String> suggestions = List.of();
            return ResponseEntity.ok(successHandler.handlerSuccess(suggestions, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all products for admin (including inactive)
     */
    @GetMapping("/admin/products")
    // // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getAllProductsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean active) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchProducts(keyword, pageRequest);
            } else if (categoryId != null) {
                products = productService.getProductsByCategory(categoryId, pageRequest);
            } else if (brandId != null && brandId != 0) {
                products = productService.getProductsByBrand(brandId, pageRequest);
            } else {
                products = productService.getAllProducts(pageRequest);
            }

            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create product (Admin only)
     */
    @PostMapping("/admin/products")
    // // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        long start = System.currentTimeMillis();
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(successHandler.handlerSuccess(createdProduct, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update product (Admin only)
     */
    @PutMapping("/admin/products/{id}")
    // // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        long start = System.currentTimeMillis();
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedProduct, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete product (Admin only)
     */
    @DeleteMapping("/admin/products/{id}")
    // // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> deleteProduct(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get featured products (Public)
     */
    @GetMapping("/products/featured")
    public ResponseEntity<BusinessApiResponse> getFeaturedProducts() {
        long start = System.currentTimeMillis();
        try {
            List<ProductDTO> products = productService.getFeaturedProducts();
            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Toggle product active status (Admin only)
     */
    @PutMapping("/admin/products/{id}/toggle-status")
    // // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> toggleProductStatus(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        try {
            ProductDTO product = productService.getProductByIdOrThrow(id);

            ProductDTO updatedProduct;
            if (product.isActive()) {
                updatedProduct = productService.deactivateProduct(id);
            } else {
                updatedProduct = productService.activateProduct(id);
            }

            return ResponseEntity.ok(successHandler.handlerSuccess(updatedProduct, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update product stock (Admin only)
     */
    @PutMapping("/admin/products/{id}/stock")
    // // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        long start = System.currentTimeMillis();
        try {
            ProductDTO updatedProduct = productService.updateStock(id, quantity);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedProduct, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}