import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/api';
import { useAuth } from './AuthContext';
import toast from '../utils/toast';

const CartContext = createContext();
export const useCart = () => useContext(CartContext);

const GUEST_CART_KEY = 'eshop_guest_cart';

/** Persist guest cart items to localStorage */
const saveGuestCart = (items) => {
  try {
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(items));
  } catch (_) {}
};

/** Load guest cart items from localStorage */
const loadGuestCart = () => {
  try {
    const raw = localStorage.getItem(GUEST_CART_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (_) {
    return [];
  }
};

/** Derive a cart-shaped object from a guest items array */
const buildGuestCartState = (items) => ({
  items,
  totalPrice: items.reduce((s, i) => s + (i.price || 0) * i.quantity, 0),
  itemCount: items.reduce((s, i) => s + i.quantity, 0)
});

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState({ items: [], totalPrice: 0, itemCount: 0 });
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  // Helper to update guest cart in both state and localStorage
  const setGuestCart = useCallback((items) => {
    const newCart = buildGuestCartState(items);
    setCart(newCart);
    saveGuestCart(items);
  }, []);

  useEffect(() => {
    console.log('🛒 CartContext - user:', user); // Debug log
    
    if (user) {
      const userRole = user.role || (user.roles && user.roles[0]);
      console.log('🛒 CartContext - User role:', userRole);
      
      if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
        console.log('🛍️ CartContext - User is admin, skipping cart fetch');
        setCart({ items: [], totalPrice: 0, itemCount: 0 });
        setLoading(false);
        return;
      }
      
      console.log('🛒 CartContext - User is customer, fetching cart...');
      fetchCart();
    } else {
      // Guest: load persisted cart from localStorage
      console.log('🛒 CartContext - No user, loading guest cart from localStorage');
      const guestItems = loadGuestCart();
      setCart(buildGuestCartState(guestItems));
      setLoading(false);
    }
  }, [user]); // eslint-disable-line react-hooks/exhaustive-deps

  const fetchCart = async () => {
    try {
      setLoading(true);
      const data = await api.getCart();
      console.log('Cart data from API:', data); // Debug log
      // BE trả về CartDTO: { id, userId, items, subtotal, totalPrice, itemCount }
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
    } catch (error) {
      console.error('Error fetching cart:', error);
      // Nếu lỗi, set cart rỗng thay vì crash
      setCart({ items: [], totalPrice: 0, itemCount: 0 });
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (productId, quantity, productMeta = {}) => {
    if (!user) {
      // Guest: merge into localStorage cart
      const current = loadGuestCart();
      const existing = current.find(i => i.productId === productId);
      let updated;
      if (existing) {
        updated = current.map(i =>
          i.productId === productId
            ? { ...i, quantity: i.quantity + quantity }
            : i
        );
      } else {
        updated = [...current, {
          id: `guest-${productId}`,
          productId,
          quantity,
          productName: productMeta.name || 'Sản phẩm',
          productImage: productMeta.imageUrl || null,
          price: productMeta.price || 0,
          stockQuantity: productMeta.stockQuantity || 99
        }];
      }
      setGuestCart(updated);
      toast.success('Thêm sản phẩm vào giỏ hàng thành công!');
      return buildGuestCartState(updated);
    }
    try {
      const data = await api.addToCart({ productId, quantity });
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
      toast.success('Thêm sản phẩm vào giỏ hàng thành công!');
      return data;
    } catch (error) {
      console.error('Error adding to cart:', error);
      if (error.message !== 'Authentication required') {
        toast.error(error.message || 'Đã xảy ra lỗi khi thêm vào giỏ hàng');
      }
      throw error;
    }
  };

  const updateItemQuantity = async (itemId, quantity) => {
    if (!user) {
      // Guest: update in localStorage
      const updated = loadGuestCart().map(i =>
        i.id === itemId ? { ...i, quantity } : i
      );
      setGuestCart(updated);
      return buildGuestCartState(updated);
    }
    try {
      console.log('🔄 Updating cart item:', itemId, 'to quantity:', quantity);
      const data = await api.updateCartItem(itemId, quantity);
      console.log('✅ Cart updated successfully:', data);
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
      return data;
    } catch (error) {
      console.error('❌ Error updating cart item:', error);
      if (error.message !== 'Authentication required') {
        toast.error(error.message || 'Đã xảy ra lỗi khi cập nhật giỏ hàng');
      }
      throw error;
    }
  };

  const removeFromCart = async (itemId) => {
    if (!user) {
      // Guest: remove from localStorage
      const updated = loadGuestCart().filter(i => i.id !== itemId);
      setGuestCart(updated);
      toast.success('Xóa sản phẩm khỏi giỏ hàng thành công!');
      return buildGuestCartState(updated);
    }
    try {
      console.log('🗑️ Removing cart item:', itemId);
      const data = await api.removeFromCart(itemId);
      console.log('✅ Cart after removal:', data);
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
      toast.success('Xóa sản phẩm khỏi giỏ hàng thành công!');
      return data;
    } catch (error) {
      console.error('❌ Error removing cart item:', error);
      if (error.message !== 'Authentication required') {
        toast.error(error.message || 'Đã xảy ra lỗi khi xóa sản phẩm');
      }
      throw error;
    }
  };

  const clearCart = async (silent = false) => {
    if (!user) {
      saveGuestCart([]);
      setCart({ items: [], totalPrice: 0, itemCount: 0 });
      if (!silent) toast.success('Xóa toàn bộ giỏ hàng thành công!');
      return;
    }
    try {
      console.log('🧹 Clearing entire cart');
      await api.clearCart();
      console.log('✅ Cart cleared');
      setCart({ items: [], totalPrice: 0, itemCount: 0 });
      if (!silent) toast.success('Xóa toàn bộ giỏ hàng thành công!');
    } catch (error) {
      console.error('❌ Error clearing cart:', error);
      if (error.message !== 'Authentication required') {
        toast.error(error.message || 'Đã xảy ra lỗi khi xóa giỏ hàng');
      }
      throw error;
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
    // BE trả về itemCount trong CartDTO
    if (cart.itemCount !== undefined) {
      return cart.itemCount;
    }
    // Fallback calculation
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
    getTotalItems,
    itemCount: cart.itemCount || 0 // Export itemCount từ BE
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
