import { API_BASE_URL } from './api';
import { parseBusinessResponse } from '../utils/responseHandler';

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

    // Parse JSON response
    const responseData = await response.json();

    if (!response.ok) {
      if (response.status === 401) {
        // Token expired or invalid - clear auth and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
        throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      }
      // Extract error message from BusinessApiResponse structure
      const errorMessage = responseData.description || responseData.message || 'Request failed';
      throw new Error(errorMessage);
    }

    // Parse BusinessApiResponse and return data
    return parseBusinessResponse(responseData);
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
  toggleProductStatus: (id) =>
    adminApi.request(`/admin/products/${id}/toggle-status`, { method: 'PUT' }),
  updateProductStock: (id, quantity) =>
    adminApi.request(`/admin/products/${id}/stock?quantity=${quantity}`, { method: 'PUT' }),
  
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
  getChatConversations: (params) => adminApi.request(`/chat/admin/conversations?${new URLSearchParams(params)}`),
  getChatMessages: (conversationId, params) => 
    adminApi.request(`/chat/admin/conversations/${conversationId}/messages?${new URLSearchParams(params)}`),
  sendChatMessage: (data) =>
    adminApi.request('/chat/admin/messages', { method: 'POST', body: JSON.stringify(data) }),
  markConversationAsRead: (conversationId) =>
    adminApi.request(`/chat/admin/conversations/${conversationId}/read`, { method: 'POST' }),
  updateConversationStatus: (conversationId, status) =>
    adminApi.request(`/chat/admin/conversations/${conversationId}/status`, { 
      method: 'PUT', 
      body: JSON.stringify({ status }) 
    }),
  assignConversation: (conversationId) =>
    adminApi.request(`/chat/admin/conversations/${conversationId}/assign`, { method: 'POST' }),
  toggleConversationAi: (conversationId, aiEnabled) =>
    adminApi.request(`/chat/admin/conversations/${conversationId}/ai-toggle`, {
      method: 'PUT',
      body: JSON.stringify({ aiEnabled })
    }),
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

  // Payment Management APIs (Admin)
  getPaymentStatistics: () => adminApi.request('/payment/statistics'),
  createRefund: (data) =>
    adminApi.request('/payment/refund', { method: 'POST', body: JSON.stringify(data) }),
  getRefundStatus: (refundId) => adminApi.request(`/payment/refund/${refundId}`),

  // Review Management APIs (Admin)
  getAllReviews: (params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/admin/reviews?${queryParams}`);
  },
  deleteReview: (reviewId) => adminApi.request(`/admin/reviews/${reviewId}`, { method: 'DELETE' }),

  // Flash Sale Management APIs (Admin)
  getFlashSales: (params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/admin/flash-sales?${queryParams}`);
  },
  getFlashSaleById: (id) => adminApi.request(`/admin/flash-sales/${id}`),
  createFlashSale: (data) =>
    adminApi.request('/admin/flash-sales', { method: 'POST', body: JSON.stringify(data) }),
  updateFlashSale: (id, data) =>
    adminApi.request(`/admin/flash-sales/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteFlashSale: (id) => adminApi.request(`/admin/flash-sales/${id}`, { method: 'DELETE' }),
  activateFlashSale: (id) =>
    adminApi.request(`/admin/flash-sales/${id}/activate`, { method: 'PUT' }),
  deactivateFlashSale: (id) =>
    adminApi.request(`/admin/flash-sales/${id}/deactivate`, { method: 'PUT' }),
  getFlashSaleProducts: (flashSaleId, params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/admin/flash-sales/${flashSaleId}/products?${queryParams}`);
  },
  addFlashSaleProduct: (flashSaleId, data) =>
    adminApi.request(`/admin/flash-sales/${flashSaleId}/products`, { 
      method: 'POST', 
      body: JSON.stringify(data) 
    }),
  updateFlashSaleProduct: (flashSaleId, productId, data) =>
    adminApi.request(`/admin/flash-sales/${flashSaleId}/products/${productId}`, { 
      method: 'PUT', 
      body: JSON.stringify(data) 
    }),
  removeFlashSaleProduct: (flashSaleId, productId) =>
    adminApi.request(`/admin/flash-sales/${flashSaleId}/products/${productId}`, { method: 'DELETE' }),
  getFlashSaleStatistics: (id) => adminApi.request(`/admin/flash-sales/${id}/statistics`),

  // Task Management APIs
  getTasks: (params = {}) => {
    const queryParams = new URLSearchParams(params);
    return adminApi.request(`/admin/tasks/list?${queryParams}`);
  },
  getTaskById: (id) => adminApi.request(`/admin/tasks/${id}`),
  createTask: (data) => adminApi.request('/admin/tasks/create', { method: 'POST', body: JSON.stringify(data) }),
  updateTask: (id, data) => adminApi.request(`/admin/tasks/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteTask: (id) => adminApi.request(`/admin/tasks/${id}`, { method: 'DELETE' }),
  updateTaskStatus: (id, status) => adminApi.request(`/admin/tasks/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
  getTaskStatistics: () => adminApi.request('/admin/tasks/statistics'),
  getMyTasks: () => adminApi.request('/admin/tasks/my'),

  // Cron Job APIs
  getCronJobs: () => adminApi.request('/admin/cron-jobs'),

  // Analytics AI APIs
  chatAnalytics: (question, sessionId) => adminApi.request('/ai/analytics', { method: 'POST', body: JSON.stringify({ question, sessionId }) }),
  getAnalyticsAiStatus: () => adminApi.request('/ai/analytics/status'),

  // AI Agent Team APIs (Phase 5)
  getAllAgentsStatus: () => adminApi.request('/ai/agents/status'),
  chatInventoryAgent: (question, sessionId) => adminApi.request('/ai/agents/inventory', { method: 'POST', body: JSON.stringify({ question, sessionId }) }),
  chatSalesAgent: (question, sessionId) => adminApi.request('/ai/agents/sales', { method: 'POST', body: JSON.stringify({ question, sessionId }) }),
  chatMarketingAgent: (question, sessionId) => adminApi.request('/ai/agents/marketing', { method: 'POST', body: JSON.stringify({ question, sessionId }) }),
  chatOrchestratorAgent: (question, sessionId) => adminApi.request('/ai/agents/orchestrator', { method: 'POST', body: JSON.stringify({ question, sessionId }) }),

  // Role Management API
  updateUserRole: (id, role) => adminApi.request(`/admin/users/${id}/role`, { method: 'PUT', body: JSON.stringify({ role }) }),

};

export default adminApi;
