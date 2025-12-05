package com.ecommerce.service.impl;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public CartDTO getCartByUserId(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Find or create cart
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        Cart cart;

        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
        } else {
            // Create new cart
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCreatedAt(new Date());
            cart.setUpdatedAt(new Date());
        }

        return convertToCartDTO(cart);
    }

    @Override
    public CartDTO addToCart(Long userId, AddToCartRequest request) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Validate product exists
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (!productOpt.isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        }
        Product product = productOpt.get();

        // Validate stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setCreatedAt(new Date());
            c.setUpdatedAt(new Date());
            return cartRepository.create(c); // MUST SAVE
        });

        // Check if product already in cart
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId()).orElse(null);

        if (item != null) {
            int newQty = item.getQuantity() + request.getQuantity();

            if (newQty > product.getStockQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            item.setQuantity(newQty);
            item.setUpdatedAt(new Date());
            cartItemRepository.update(item); // MUST SAVE UPDATE
        } else {
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getSalePrice() != null ? product.getSalePrice() : product.getPrice());
            newItem.setCreatedAt(new Date());
            newItem.setUpdatedAt(new Date());

            cartItemRepository.create(newItem); // MUST SAVE NEW
        }

        cart.setUpdatedAt(new Date());
        cartRepository.update(cart);

        // Reload cart including items
        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        return convertToCartDTO(updatedCart);
    }

    @Override
    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }

        // Validate product exists and stock
        Optional<Product> productOpt = productRepository.findById(cartItem.getProductId());
        if (!productOpt.isPresent()) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productOpt.get();

        if (quantity > product.getStockQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cartItemRepository.deleteByCartIdAndProductId(cartItem.getCartId(), cartItem.getProductId());
        } else {
            // Update quantity in database
            Date now = new Date();
            cartItemRepository.updateCartItem(cartItemId, quantity, now);
        }

        // Get updated cart
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.setUpdatedAt(new Date());
        }

        return convertToCartDTO(cart);
    }

    private void removeFromCart(Long userId, Long cartItemId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }

        // Remove item
        cartItemRepository.deleteByCartIdAndProductId(cartItem.getCartId(), cartItem.getProductId());

        // Update cart timestamp
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.setUpdatedAt(new Date());
        }
    }

    @Override
    public void clearCart(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Find cart and clear all items
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setUpdatedAt(new Date());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateCartTotal(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Find cart
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (!cartOptional.isPresent()) {
            return 0.0;
        }

        Cart cart = cartOptional.get();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        return cartItems.stream()
                .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        removeFromCart(userId, cartItemId);
    }

    private CartDTO convertToCartDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUserId());
        cartDTO.setSessionId(cart.getSessionId());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setUpdatedAt(cart.getUpdatedAt());

        // Load cart items
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        List<CartItemDTO> itemDTOs = cartItems.stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());

        cartDTO.setItems(itemDTOs);

        // Calculate totals
        BigDecimal subtotal = itemDTOs.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cartDTO.setSubtotal(subtotal);
        cartDTO.setTotalPrice(subtotal);
        cartDTO.setItemCount(itemDTOs.size());

        return cartDTO;
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCartId());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        dto.setSubtotal(cartItem.getSubTotal());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());

        // Load product information
        Optional<Product> productOpt = productRepository.findById(cartItem.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            dto.setProductName(product.getName());
            dto.setProductSku(product.getSku());
            dto.setProductImage(product.getImageUrl());
            dto.setStockQuantity(product.getStockQuantity()); // Add stock info for FE validation
            // Lazy load images if needed
            dto.setProductImage(productRepository.findImagesByProductId(product.getId()).stream()
                    .filter(img -> img.getImageUrl() != null)
                    .map(img -> img.getImageUrl()).collect(Collectors.toList()).get(0));
            // dto.setProductImages(productRepository.findImagesByProductId(product.getId()));
        }

        return dto;
    }
}
