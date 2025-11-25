package com.ecommerce.service;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.AddToCartRequest;

public interface CartService {
    CartDTO getCartByUserId(Long userId);

    CartDTO addToCart(Long userId, AddToCartRequest request);

    CartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);

    void removeCartItem(Long userId, Long cartItemId);

    void clearCart(Long userId);

    Double calculateCartTotal(Long userId);
}