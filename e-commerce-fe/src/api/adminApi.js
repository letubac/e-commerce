import { API_BASE_URL } from './api';

const adminApi = {
  baseUrl: API_BASE_URL,

  async request(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
      },
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Request failed');
    }

    return response.json();
  },

  // Dashboard APIs
  getDashboardOverview: () => adminApi.request('/dashboard/overview'),
  getSalesStatistics: (days = 7) => adminApi.request(`/dashboard/sales?days=${days}`),
  getUserStatistics: () => adminApi.request('/dashboard/users'),
  getProductStatistics: () => adminApi.request('/dashboard/products'),
  getOrderStatistics: () => adminApi.request('/dashboard/orders'),
  getRecentActivities: (limit = 20) => adminApi.request(`/dashboard/activities?limit=${limit}`),
  getSystemHealth: () => adminApi.request('/dashboard/health'),
  
  // Order Management APIs
  getAllOrders: (params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/orders/admin/all?${queryParams}`);
  },
  getOrdersByStatus: (status, params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/orders/admin/status/${status}?${queryParams}`);
  },
  getOrderDetails: (id) => adminApi.request(`/orders/admin/${id}`),
  updateOrderStatus: (id, status) =>
    adminApi.request(`/orders/admin/${id}/status?status=${status}`, { method: 'PUT' }),
  updateTrackingNumber: (id, trackingNumber) =>
    adminApi.request(`/orders/admin/${id}/tracking`, { 
      method: 'PUT', 
      body: JSON.stringify({ trackingNumber }) 
    }),
  
  // Product Management APIs
  getProducts: (params) => adminApi.request(`/admin/products?${new URLSearchParams(params)}`),
  createProduct: (data) =>
    adminApi.request('/admin/products', { method: 'POST', body: JSON.stringify(data) }),
  updateProduct: (id, data) =>
    adminApi.request(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteProduct: (id) => adminApi.request(`/admin/products/${id}`, { method: 'DELETE' }),
  
  // User Management APIs
  getUsers: (params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/admin/users?${queryParams}`);
  },
  getUserById: (id) => adminApi.request(`/admin/users/${id}`),
  lockUser: (id) => adminApi.request(`/admin/users/${id}/lock`, { method: 'PUT' }),
  unlockUser: (id) => adminApi.request(`/admin/users/${id}/unlock`, { method: 'PUT' }),
  getUserStatisticsDetailed: () => adminApi.request('/admin/users/statistics'),
  
  // Coupon Management APIs
  getCoupons: (params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/admin/coupons?${queryParams}`);
  },
  getCouponById: (id) => adminApi.request(`/admin/coupons/${id}`),
  createCoupon: (data) =>
    adminApi.request('/admin/coupons', { method: 'POST', body: JSON.stringify(data) }),
  updateCoupon: (id, data) =>
    adminApi.request(`/admin/coupons/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCoupon: (id) => adminApi.request(`/admin/coupons/${id}`, { method: 'DELETE' }),
  
  // Category Management APIs
  getCategories: () => adminApi.request('/admin/categories'),
  getCategoryById: (id) => adminApi.request(`/admin/categories/${id}`),
  createCategory: (data) =>
    adminApi.request('/admin/categories', { method: 'POST', body: JSON.stringify(data) }),
  updateCategory: (id, data) =>
    adminApi.request(`/admin/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCategory: (id) => adminApi.request(`/admin/categories/${id}`, { method: 'DELETE' }),
  
  // Brand Management APIs
  getBrands: () => adminApi.request('/admin/brands'),
  getBrandById: (id) => adminApi.request(`/admin/brands/${id}`),
  createBrand: (data) =>
    adminApi.request('/admin/brands', { method: 'POST', body: JSON.stringify(data) }),
  updateBrand: (id, data) =>
    adminApi.request(`/admin/brands/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteBrand: (id) => adminApi.request(`/admin/brands/${id}`, { method: 'DELETE' }),

  // Chat Management APIs
  getChatConversations: (params) => adminApi.request(`/chat/conversations?${new URLSearchParams(params)}`),
  getChatMessages: (conversationId, params) => 
    adminApi.request(`/chat/conversations/${conversationId}/messages?${new URLSearchParams(params)}`),
  sendChatMessage: (data) =>
    adminApi.request('/chat/messages', { method: 'POST', body: JSON.stringify(data) }),
  markConversationAsRead: (conversationId) =>
    adminApi.request(`/chat/conversations/${conversationId}/read`, { method: 'POST' }),
  updateConversationStatus: (conversationId, status) =>
    adminApi.request(`/chat/conversations/${conversationId}/status`, { 
      method: 'PUT', 
      body: JSON.stringify({ status }) 
    }),
  assignConversation: (conversationId) =>
    adminApi.request(`/chat/conversations/${conversationId}/assign`, { method: 'POST' }),
  getChatQuickReplies: () => adminApi.request('/chat/quick-replies'),
  createChatQuickReply: (data) =>
    adminApi.request('/chat/quick-replies', { method: 'POST', body: JSON.stringify(data) }),
  deleteChatQuickReply: (id) => adminApi.request(`/chat/quick-replies/${id}`, { method: 'DELETE' }),
  uploadChatFile: (formData) =>
    adminApi.request('/chat/upload', { 
      method: 'POST', 
      body: formData,
      headers: {} // Remove Content-Type to allow multipart/form-data boundary
    }),
  getChatStatistics: () => adminApi.request('/chat/statistics'),
  getChatSettings: () => adminApi.request('/chat/settings'),
  updateChatSettings: (data) =>
    adminApi.request('/chat/settings', { method: 'PUT', body: JSON.stringify(data) }),
};

export default adminApi;
