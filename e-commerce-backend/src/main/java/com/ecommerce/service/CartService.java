package com.ecommerce.service;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.exception.DetailException;

/**
 * author: LeTuBac
 */
public interface CartService {
    CartDTO getCartByUserId(Long userId) throws DetailException;

    CartDTO addToCart(Long userId, AddToCartRequest request) throws DetailException;

    CartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) throws DetailException;

    void removeCartItem(Long userId, Long cartItemId) throws DetailException;

    void clearCart(Long userId) throws DetailException;

    Double calculateCartTotal(Long userId) throws DetailException;
}