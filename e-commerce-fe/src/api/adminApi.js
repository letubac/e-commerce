const adminApi = {
  baseUrl: 'http://localhost:8080/api/v1',

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

  getOverview: () => adminApi.request('/dashboard/overview'),
  getOrders: (params) => adminApi.request(`/orders?${new URLSearchParams(params)}`),
  updateOrderStatus: (id, status) =>
    adminApi.request(`/orders/${id}/status?status=${status}`, { method: 'PUT' }),
  getProducts: (params) => adminApi.request(`/admin/products?${new URLSearchParams(params)}`),
  createProduct: (data) =>
    adminApi.request('/admin/products', { method: 'POST', body: JSON.stringify(data) }),
  updateProduct: (id, data) =>
    adminApi.request(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteProduct: (id) => adminApi.request(`/admin/products/${id}`, { method: 'DELETE' }),
  getUsers: (params) => adminApi.request(`/users?${new URLSearchParams(params)}`),
  lockUser: (id) => adminApi.request(`/users/${id}/lock`, { method: 'PUT' }),
  unlockUser: (id) => adminApi.request(`/users/${id}/unlock`, { method: 'PUT' }),
  getCoupons: (params) => adminApi.request(`/coupons?${new URLSearchParams(params)}`),
  createCoupon: (data) =>
    adminApi.request('/coupons', { method: 'POST', body: JSON.stringify(data) }),
  deleteCoupon: (id) => adminApi.request(`/coupons/${id}`, { method: 'DELETE' }),
  getCategories: () => adminApi.request('/admin/categories'),
  getBrands: () => adminApi.request('/admin/brands'),

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
