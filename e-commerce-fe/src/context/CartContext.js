import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/api';
import { useAuth } from './AuthContext';
import toast from '../utils/toast';

const CartContext = createContext();
export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState({ items: [], totalPrice: 0, itemCount: 0 });
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    console.log('🛒 CartContext - user:', user); // Debug log
    
    // Chỉ fetch cart nếu user là CUSTOMER, không fetch cho ADMIN
    if (user) {
      const userRole = user.role || (user.roles && user.roles[0]);
      console.log('🛒 CartContext - User role:', userRole);
      
      if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
        console.log('🛍️ CartContext - User is admin, skipping cart fetch');
        setCart({ items: [], totalPrice: 0, itemCount: 0 });
        setLoading(false);
        return;
      }
      
      console.log('🛒 CartContext - User is customer, fetching cart...'); // Debug log
      fetchCart();
    } else {
      console.log('🛒 CartContext - No user, skipping cart fetch'); // Debug log
      setLoading(false);
    }
  }, [user]);

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

  const addToCart = async (productId, quantity) => {
    try {
      // BE trả về CartDTO sau khi thêm item
      const data = await api.addToCart({ productId, quantity });
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
      toast.success('Thêm sản phẩm vào giỏ hàng thành công!');
      return data;
    } catch (error) {
      console.error('Error adding to cart:', error);
      if (error.message !== 'Authentication required') {
        // Error message đã được i18n từ BE
        toast.error(error.message || 'Đã xảy ra lỗi khi thêm vào giỏ hàng');
      }
      throw error;
    }
  };

  const updateItemQuantity = async (itemId, quantity) => {
    try {
      console.log('🔄 Updating cart item:', itemId, 'to quantity:', quantity); // Debug
      // BE trả về CartDTO sau khi update quantity
      const data = await api.updateCartItem(itemId, quantity);
      console.log('✅ Cart updated successfully:', data); // Debug
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
      return data;
    } catch (error) {
      console.error('❌ Error updating cart item:', error);
      if (error.message !== 'Authentication required') {
        // Error message đã được i18n từ BE (VD: "Không đủ số lượng tồn kho")
        toast.error(error.message || 'Đã xảy ra lỗi khi cập nhật giỏ hàng');
      }
      throw error;
    }
  };

  const removeFromCart = async (itemId) => {
    try {
      console.log('🗑️ Removing cart item:', itemId); // Debug
      // BE trả về CartDTO sau khi xóa item
      const data = await api.removeFromCart(itemId);
      console.log('✅ Cart after removal:', data); // Debug
      setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
      toast.success('Xóa sản phẩm khỏi giỏ hàng thành công!');
      return data;
    } catch (error) {
      console.error('❌ Error removing cart item:', error);
      if (error.message !== 'Authentication required') {
        // Error message đã được i18n từ BE
        toast.error(error.message || 'Đã xảy ra lỗi khi xóa sản phẩm');
      }
      throw error;
    }
  };

  const clearCart = async (silent = false) => {
    try {
      console.log('🧹 Clearing entire cart'); // Debug
      // BE trả về void, set cart rỗng
      await api.clearCart();
      console.log('✅ Cart cleared'); // Debug
      setCart({ items: [], totalPrice: 0, itemCount: 0 });
      if (!silent) {
        toast.success('Xóa toàn bộ giỏ hàng thành công!');
      }
    } catch (error) {
      console.error('❌ Error clearing cart:', error);
      if (error.message !== 'Authentication required') {
        // Error message đã được i18n từ BE
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
