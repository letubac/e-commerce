# Category Module Refactoring - Quick Completion Guide

## ✅ Completed:
1. ✅ CategoryConstant created (E400-E449, S400-S429)
2. ✅ messages_vi.properties - All Category messages added
3. ✅ messages_en.properties - All Category messages added
4. ✅ CategoryService interface - Added `throws DetailException` to all methods
5. ✅ CategoryServiceImpl - Refactored all methods to throw DetailException
6. ⚠️ CategoryController - **PARTIALLY DONE** - Need to complete remaining endpoints

## 🔄 Remaining Work - CategoryController:

Run these replacements manually or use find/replace:

### 1. Fix remaining endpoints that still use ApiResponse:

```java
// getCategoryProducts - Line ~64
@GetMapping("/categories/{categoryId}/products")
public ResponseEntity<BusinessApiResponse> getCategoryProducts(...) {
    long start = System.currentTimeMillis();
    try {
        categoryService.findById(categoryId);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ProductDTO> products = productService.findByCategoryId(categoryId, pageRequest, minPrice, maxPrice, brandId, active);
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S420_CATEGORY_PRODUCTS_LISTED, products, start));
    } catch (Exception e) {
        log.error("Error fetching products for category ID: {}", categoryId, e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}

// getAllCategoriesAdmin - Line ~100
@GetMapping("/admin/categories")
public ResponseEntity<BusinessApiResponse> getAllCategoriesAdmin() {
    long start = System.currentTimeMillis();
    try {
        List<CategoryDTO> categories = categoryService.findAll();
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S401_CATEGORIES_LISTED, categories, start));
    } catch (Exception e) {
        log.error("Error fetching all categories for admin", e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}

// createCategory - Line ~116
@PostMapping("/admin/categories")
public ResponseEntity<BusinessApiResponse> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
    long start = System.currentTimeMillis();
    try {
        CategoryDTO createdCategory = categoryService.save(categoryDTO);
        log.info("Created category: {}", createdCategory.getName());
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S405_CATEGORY_CREATED, createdCategory, start));
    } catch (Exception e) {
        log.error("Error creating category", e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}

// updateCategory - Line ~137
@PutMapping("/admin/categories/{id}")
public ResponseEntity<BusinessApiResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
    long start = System.currentTimeMillis();
    try {
        categoryDTO.setId(id);
        CategoryDTO updatedCategory = categoryService.update(categoryDTO);
        log.info("Updated category ID: {}", id);
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S406_CATEGORY_UPDATED, updatedCategory, start));
    } catch (Exception e) {
        log.error("Error updating category ID: {}", id, e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}

// deleteCategory - Line ~164
@DeleteMapping("/admin/categories/{id}")
public ResponseEntity<BusinessApiResponse> deleteCategory(@PathVariable Long id) {
    long start = System.currentTimeMillis();
    try {
        categoryService.deleteById(id);
        log.info("Deleted category ID: {}", id);
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S407_CATEGORY_DELETED, null, start));
    } catch (Exception e) {
        log.error("Error deleting category ID: {}", id, e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}

// getCategoryById - Line ~181
@GetMapping("/admin/categories/{id}")
public ResponseEntity<BusinessApiResponse> getCategoryById(@PathVariable Long id) {
    long start = System.currentTimeMillis();
    try {
        CategoryDTO category = categoryService.findById(id);
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S403_CATEGORY_DETAILS_RETRIEVED, category, start));
    } catch (Exception e) {
        log.error("Error fetching category ID: {}", id, e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}

// toggleCategoryStatus - Line ~197
@PutMapping("/admin/categories/{id}/toggle-status")
public ResponseEntity<BusinessApiResponse> toggleCategoryStatus(@PathVariable Long id) {
    long start = System.currentTimeMillis();
    try {
        CategoryDTO updatedCategory = categoryService.toggleActiveStatus(id);
        log.info("Toggled category status ID: {} to {}", id, updatedCategory.isActive());
        return ResponseEntity.ok(successHandler.handlerSuccess(CategoryConstant.S408_CATEGORY_STATUS_TOGGLED, updatedCategory, start));
    } catch (Exception e) {
        log.error("Error toggling category status ID: {}", id, e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}
```

## 📝 Next: Frontend Refactoring

Once backend is complete, refactor frontend AdminCategoryManagement component or similar to handle BusinessApiResponse structure.

Key changes needed in FE:
1. API calls already use `parseBusinessResponse()` 
2. Error messages will be i18n from BE automatically
3. No additional FE changes may be needed if using api.js properly

## 🎯 Pattern Summary

**Backend Pattern:**
```java
@GetMapping("/endpoint")
public ResponseEntity<BusinessApiResponse> methodName(...) {
    long start = System.currentTimeMillis();
    try {
        // Business logic
        Data result = service.method(...);
        return ResponseEntity.ok(successHandler.handlerSuccess(SUCCESS_CODE, result, start));
    } catch (Exception e) {
        log.error("Error message", e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}
```

**Frontend Pattern:**
Already implemented in api.js - automatic parsing via `parseBusinessResponse()`
