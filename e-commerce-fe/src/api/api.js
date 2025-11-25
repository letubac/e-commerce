const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = {
  async request(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
      });

      if (!response.ok) {
        let errorMessage = 'Request failed';
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || `HTTP ${response.status}`;
        } catch (e) {
          errorMessage = `HTTP ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return response.json();
      } else {
        return response.text();
      }
    } catch (error) {
      console.error('API Request Error:', error);
      throw error;
    }
  },

  // Auth
  register: (data) => api.request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => {
    console.log('Login data being sent:', data); // Debug log
    return api.request('/auth/login', { method: 'POST', body: JSON.stringify(data) });
  },
  verify2FA: (data) => api.request('/auth/verify-2fa', { method: 'POST', body: JSON.stringify(data) }),
  getCurrentUser: () => api.request('/auth/me'),

  // Products
  getProducts: (params) => {
    // Tách sortBy/sortDirection nếu có, loại bỏ sort cũ nếu còn
    const query = { ...params };
    if (query.sort) {
      // Nếu FE còn truyền sort, tách ra sortBy/sortDirection
      const [field, direction] = query.sort.split(',');
      query.sortBy = field || 'createdAt';
      query.sortDirection = direction || 'desc';
      delete query.sort;
    }

    const sanitizedParams = api.sanitizeParams ? api.sanitizeParams(query) : sanitizeParams(query); // Sanitize params to remove "null" values

    // Ensure no "null" values are sent for Long-type parameters
    const queryParams = new URLSearchParams(sanitizedParams);
    if (queryParams.get("brandId") === "null") {
        queryParams.delete("brandId");
    }

    return api.request(`/products?${queryParams.toString()}`);
  },
  getProduct: (id) => api.request(`/products/${id}`),
  getProductDetails: (id) => api.request(`/products/${id}`),
  searchProducts: (keyword, params = {}) => api.request(`/products/search?keyword=${encodeURIComponent(keyword)}&${new URLSearchParams(params)}`),
  searchSuggestions: (keyword) => api.request(`/products/search/suggestions?keyword=${keyword}`),
  
  // Admin Product Management
  getAllProductsAdmin: (params) => api.request(`/admin/products?${new URLSearchParams(params)}`),
  createProduct: (data) => api.request('/admin/products', { method: 'POST', body: JSON.stringify(data) }),
  updateProduct: (id, data) => api.request(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteProduct: (id) => api.request(`/admin/products/${id}`, { method: 'DELETE' }),
  
  // Categories
  getCategories: () => api.request('/categories'),
  getCategoryProducts: (categoryId, params = {}) => api.request(`/categories/${categoryId}/products?${new URLSearchParams(params)}`),
  
  // Admin Categories Management
  getAllCategoriesAdmin: () => api.request('/admin/categories'),
  createCategory: (data) => api.request('/admin/categories', { method: 'POST', body: JSON.stringify(data) }),
  updateCategory: (id, data) => api.request(`/admin/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCategory: (id) => api.request(`/admin/categories/${id}`, { method: 'DELETE' }),
  
  // Brands
  getBrands: () => api.request('/brands'),
  getBrandProducts: (brandId, params = {}) => api.request(`/brands/${brandId}/products?${new URLSearchParams(params)}`),
  
  // Admin Brands Management
  getAllBrandsAdmin: () => api.request('/admin/brands'),
  createBrand: (data) => api.request('/admin/brands', { method: 'POST', body: JSON.stringify(data) }),
  updateBrand: (id, data) => api.request(`/admin/brands/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteBrand: (id) => api.request(`/admin/brands/${id}`, { method: 'DELETE' }),
  
  // Reviews
  getProductReviews: (productId, params = {}) => api.request(`/products/${productId}/reviews?${new URLSearchParams(params)}`),
  createReview: (productId, data) => api.request(`/products/${productId}/reviews`, { method: 'POST', body: JSON.stringify(data) }),
  updateReview: (reviewId, data) => api.request(`/reviews/${reviewId}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteReview: (reviewId) => api.request(`/reviews/${reviewId}`, { method: 'DELETE' }),

  // Cart
  getCart: () => api.request('/cart'),
  addToCart: (data) => api.request('/cart/items', { method: 'POST', body: JSON.stringify(data) }),
  updateCartItem: (id, quantity) => api.request(`/cart/items/${id}?quantity=${quantity}`, { method: 'PUT' }),
  removeFromCart: (id) => api.request(`/cart/items/${id}`, { method: 'DELETE' }),

  // Orders
  createOrder: (data) => api.request('/orders', { method: 'POST', body: JSON.stringify(data) }),
  getOrders: () => api.request('/orders'),

  // Payment
  createPaymentIntent: (data) => api.request('/payments/create-intent', { method: 'POST', body: JSON.stringify(data) }),

  // Chat APIs
  getUserConversations: () => api.request('/chat/conversations'),
  getConversationMessages: (conversationId) => api.request(`/chat/conversations/${conversationId}/messages`),
  sendMessage: (data) => api.request('/chat/messages', { method: 'POST', body: JSON.stringify(data) }),
  createConversation: (data) => api.request('/chat/conversations', { method: 'POST', body: JSON.stringify(data) }),
  uploadChatFile: (formData) => api.request('/chat/upload', { 
    method: 'POST', 
    body: formData,
    headers: {} // Remove Content-Type to allow multipart/form-data boundary
  }),
  markMessagesAsRead: (conversationId) => api.request(`/chat/conversations/${conversationId}/read`, { method: 'POST' }),
  getChatStatus: () => api.request('/chat/status'),
};

export const sanitizeParams = (params) => {
    const sanitizedParams = { ...params };

    // Remove "null" values from query parameters
    Object.keys(sanitizedParams).forEach((key) => {
        if (sanitizedParams[key] === "null") {
            delete sanitizedParams[key];
        }
    });

    return sanitizedParams;
};

export const fetchProducts = async (params) => {
    // Sử dụng api.getProducts thay vì axios để tránh phụ thuộc axios
    const data = await api.getProducts(params);
    return data;
};


export default api;
