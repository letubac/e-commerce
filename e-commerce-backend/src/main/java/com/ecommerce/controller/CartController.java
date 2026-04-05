package com.ecommerce.controller;

import com.ecommerce.constant.CartConstant;
import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.exception.DetailException;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import com.ecommerce.webapp.BusinessApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart")
/**
 * author: LeTuBac
 */
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    @GetMapping
    public ResponseEntity<BusinessApiResponse> getCart(Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            // Kiểm tra role - chỉ CUSTOMER/USER mới có giỏ hàng
            checkCustomerRole(authentication);

            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO cart = cartService.getCartByUserId(userId);

            return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
        } catch (Exception e) {
            log.error("Error getting cart", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<BusinessApiResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            checkCustomerRole(authentication);
            Long userId = getUserIdFromAuthentication(authentication);

            CartDTO cart = cartService.addToCart(userId, request);

            return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
        } catch (Exception e) {
            log.error("Error adding to cart", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<BusinessApiResponse> updateCartItem(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(name = "quantity") Integer quantity,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            checkCustomerRole(authentication);
            Long userId = getUserIdFromAuthentication(authentication);

            CartDTO cart = cartService.updateCartItemQuantity(userId, itemId, quantity);

            return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
        } catch (Exception e) {
            log.error("Error updating cart item", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<BusinessApiResponse> removeCartItem(
            @PathVariable(name = "itemId") Long itemId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            checkCustomerRole(authentication);
            Long userId = getUserIdFromAuthentication(authentication);

            cartService.removeCartItem(userId, itemId);
            CartDTO cart = cartService.getCartByUserId(userId);

            return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
        } catch (Exception e) {
            log.error("Error removing cart item", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @DeleteMapping
    public ResponseEntity<BusinessApiResponse> clearCart(Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            checkCustomerRole(authentication);
            Long userId = getUserIdFromAuthentication(authentication);

            cartService.clearCart(userId);
            CartDTO cart = cartService.getCartByUserId(userId);

            return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    private void checkCustomerRole(Authentication authentication) throws DetailException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isAdmin) {
            throw new DetailException(CartConstant.E306_ADMIN_NO_CART);
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId(); // getId() trả về Long userId
    }
}