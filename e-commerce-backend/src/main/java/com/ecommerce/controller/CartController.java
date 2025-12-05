package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(Authentication authentication) {
        // Kiểm tra role - chỉ CUSTOMER/USER mới có giỏ hàng
        checkCustomerRole(authentication);

        Long userId = getUserIdFromAuthentication(authentication);
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        checkCustomerRole(authentication);
        Long userId = getUserIdFromAuthentication(authentication);
        CartDTO cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        checkCustomerRole(authentication);
        Long userId = getUserIdFromAuthentication(authentication);
        CartDTO cart = cartService.updateCartItemQuantity(userId, itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        checkCustomerRole(authentication);
        Long userId = getUserIdFromAuthentication(authentication);
        cartService.removeCartItem(userId, itemId);
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<CartDTO> clearCart(Authentication authentication) {
        checkCustomerRole(authentication);
        Long userId = getUserIdFromAuthentication(authentication);
        cartService.clearCart(userId);
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    private void checkCustomerRole(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isAdmin) {
            throw new RuntimeException("Admin không có giỏ hàng");
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId(); // getId() trả về Long userId
    }
}