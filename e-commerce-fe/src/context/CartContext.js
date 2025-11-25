import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/api';
import { useAuth } from './AuthContext';

const CartContext = createContext();
export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState({ items: [], totalPrice: 0, totalItems: 0 });
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      fetchCart();
    } else {
      setLoading(false);
    }
  }, [user]);

  const fetchCart = async () => {
    try {
      setLoading(true);
      const data = await api.getCart();
      console.log('Cart data from API:', data); // Debug log
      setCart(data);
    } catch (error) {
      console.error('Error fetching cart:', error);
      // Nếu lỗi, set cart rỗng thay vì crash
      setCart({ items: [], totalPrice: 0, totalItems: 0 });
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (productId, quantity) => {
    try {
      const data = await api.addToCart({ productId, quantity });
      setCart(data);
    } catch (error) {
      alert(error.message);
    }
  };

  const updateItemQuantity = async (itemId, quantity) => {
    try {
      const data = await api.updateCartItem(itemId, quantity);
      setCart(data);
    } catch (error) {
      console.error('Error updating cart item:', error);
      alert(error.message);
    }
  };

  const removeFromCart = async (itemId) => {
    try {
      const data = await api.removeFromCart(itemId);
      setCart(data);
    } catch (error) {
      console.error('Error removing cart item:', error);
      alert(error.message);
    }
  };

  const clearCart = async () => {
    try {
      // API call to clear cart or remove each item
      for (const item of cart.items || []) {
        await api.removeFromCart(item.id);
      }
      setCart({ items: [], totalPrice: 0, totalItems: 0 });
    } catch (error) {
      console.error('Error clearing cart:', error);
      alert(error.message);
    }
  };

  const getTotalPrice = () => {
    if (cart.totalPrice) {
      return cart.totalPrice;
    }
    // Fallback calculation if API doesn't provide totalPrice
    return (cart.items || []).reduce((total, item) => {
      const price = item.product?.price || item.price || 0;
      return total + (price * item.quantity);
    }, 0);
  };

  const getTotalItems = () => {
    if (cart.totalItems) {
      return cart.totalItems;
    }
    // Fallback calculation if API doesn't provide totalItems
    return (cart.items || []).reduce((total, item) => total + item.quantity, 0);
  };

  const value = {
    cartItems: cart.items || [],
    cart,
    loading,
    fetchCart,
    addToCart,
    updateItemQuantity,
    removeFromCart,
    clearCart,
    getTotalPrice,
    getTotalItems
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
