import toast from '../utils/toast';
import { parseBusinessResponse } from '../utils/responseHandler';

// ─── FE Search Cache (Item #0) ────────────────────────────────────────────────
// Simple in-memory cache với TTL 5 phút cho các request tìm kiếm sản phẩm
const _searchCache = new Map(); // key → { data, expiry }
const SEARCH_CACHE_TTL = 5 * 60 * 1000; // 5 phút

const getCached = (key) => {
  const entry = _searchCache.get(key);
  if (!entry) return null;
  if (Date.now() > entry.expiry) { _searchCache.delete(key); return null; }
  return entry.data;
};
const setCache = (key, data) => {
  _searchCache.set(key, { data, expiry: Date.now() + SEARCH_CACHE_TTL });
};
// ─────────────────────────────────────────────────────────────────────────────

export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;
// REACT_APP_IMAGE_BASE_URL overrides the default (used in prod to point to Supabase Storage)
export const IMAGE_BASE_URL = process.env.REACT_APP_IMAGE_BASE_URL || `${process.env.REACT_APP_API_BASE_URL}/files`;

// Helper function to get full image URL
export const getImageUrl = (imageUrl) => {
  if (!imageUrl || typeof imageUrl !== 'string') {
    return null;
  }
  if (imageUrl.startsWith('http')) {
    return imageUrl;
  }
  const fullUrl = `${IMAGE_BASE_URL}${imageUrl}`;
  return fullUrl;
};

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

      // Parse JSON response first
      const contentType = response.headers.get('content-type');
      let responseData;
      
      if (contentType && contentType.includes('application/json')) {
        responseData = await response.json();
      } else {
        responseData = await response.text();
      }

      // Check if response is not ok
      if (!response.ok) {
        let errorMessage = 'Request failed';
        
        // Try to extract error from BusinessApiResponse structure
        if (responseData && typeof responseData === 'object') {
          errorMessage = responseData.description || responseData.message || responseData.error || `HTTP ${response.status}`;
        } else if (typeof responseData === 'string') {
          errorMessage = responseData || `HTTP ${response.status}: ${response.statusText}`;
        }

        // Handle 401 Unauthorized - token expired or invalid
        if (response.status === 401) {
          console.error('401 Unauthorized - Token expired or invalid');
          
          // Only clear auth data if this is NOT the login endpoint itself
          // (login returns 401 for wrong credentials — don't wipe unrelated state)
          if (!endpoint.includes('/auth/login')) {
            const hadToken = localStorage.getItem('token');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            // Only toast if the user actually had a session (not on fresh anonymous page loads)
            if (hadToken) {
              toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            }
          }
          
          throw new Error('Authentication required');
        }

        // For other errors, log and throw
        console.error(`API Error [${response.status}]:`, errorMessage);
        throw new Error(errorMessage);
      }

      // Parse BusinessApiResponse and return data
      // This will extract data from { codeStatus, messageStatus, description, data }
      return parseBusinessResponse(responseData);
      
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
  getProducts: async (params = {}) => {
    // Tách sortBy/sortDirection nếu có, loại bỏ sort cũ nếu còn
    const query = { ...params };
    if (query.sort) {
      // Nếu FE còn truyền sort, tách ra sortBy/sortDirection
      const [field, direction] = query.sort.split(',');
      query.sortBy = field || 'createdAt';
      query.sortDirection = direction || 'desc';
      delete query.sort;
    }

    // BE ProductController nhận 'keyword', FE gửi 'search' — map lại đúng tên param
    if (query.search !== undefined) {
      if (query.search) query.keyword = query.search;
      delete query.search;
    }

    const sanitizedParams = api.sanitizeParams ? api.sanitizeParams(query) : sanitizeParams(query); // Sanitize params to remove "null" values

    // Ensure no "null" values are sent for Long-type parameters
    const queryParams = new URLSearchParams(sanitizedParams);
    if (queryParams.get("brandId") === "null") {
        queryParams.delete("brandId");
    }
    if (queryParams.get("categoryId") === "null") {
      queryParams.delete("categoryId");
    }

    // Gọi API và normalize response (dùng cache nếu có)
    const cacheKey = `getProducts:${queryParams.toString()}`;
    const cached = getCached(cacheKey);
    const res = cached ?? await api.request(`/products?${queryParams.toString()}`);
    if (!cached) setCache(cacheKey, res);
    // parseBusinessResponse đã trả về data, có thể là array hoặc pagination object
    const items = res?.content ?? res?.items ?? (Array.isArray(res) ? res : []);
    const totalPages = res?.totalPages ?? null;
    const totalElements = res?.totalElements ?? res?.total ?? null;

    return { items, totalPages, totalElements, raw: res };
  },

  getProductsByCategory: async (categoryId, params = {}) => {
    const query = { ...params, categoryId };
    const queryParams = new URLSearchParams(query);
    const res = await api.request(`/products?${queryParams.toString()}`);
    return res; // Return full response with pagination
  },

  getProduct: (id) => api.request(`/products/${id}`),
  getProductDetails: (id) => api.request(`/products/${id}`),
  searchProducts: async (keyword, params = {}) => {
    const qs = `keyword=${encodeURIComponent(keyword)}&${new URLSearchParams(params)}`;
    const cacheKey = `searchProducts:${qs}`;
    const cached = getCached(cacheKey);
    if (cached) return cached;
    const data = await api.request(`/products/search?${qs}`);
    setCache(cacheKey, data);
    return data;
  },
  searchSuggestions: async (keyword) => {
    const cacheKey = `searchSuggestions:${keyword}`;
    const cached = getCached(cacheKey);
    if (cached) return cached;
    const data = await api.request(`/products/search/suggestions?keyword=${keyword}`);
    setCache(cacheKey, data);
    return data;
  },
  getFeaturedProducts: () => api.request('/products/featured'),
  
  // Admin Product Management
  getAllProductsAdmin: (params) => api.request(`/admin/products?${new URLSearchParams(params)}`),
  createProduct: (data) => api.request('/admin/products', { method: 'POST', body: JSON.stringify(data) }),
  updateProduct: (id, data) => api.request(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteProduct: (id) => api.request(`/admin/products/${id}`, { method: 'DELETE' }),

  // Upload image file — returns { filePath, fileUrl, fileName, fileSize, contentType }
  uploadImage: async (file, category = 'products') => {
    const token = localStorage.getItem('token');
    const fd = new FormData();
    fd.append('file', file);
    fd.append('category', category);
    const response = await fetch(`${API_BASE_URL}/admin/upload`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: fd,
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || `Upload failed: ${response.status}`);
    }
    const json = await response.json();
    return json.data; // { filePath, fileUrl, fileName, fileSize, contentType }
  },
  
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
  clearCart: () => api.request('/cart', { method: 'DELETE' }),

  // Orders
  createOrder: (data) => api.request('/orders', { method: 'POST', body: JSON.stringify(data) }),
  getOrders: () => api.request('/orders'),
  cancelOrder: (orderId) => api.request(`/orders/${orderId}/cancel`, { method: 'PUT' }),

  // Payment APIs
  getPaymentMethods: () => parseBusinessResponse(api.request('/payment/methods')),
  processPayment: (data) => parseBusinessResponse(api.request('/payment/process', { method: 'POST', body: JSON.stringify(data) })),
  verifyPayment: (data) => parseBusinessResponse(api.request('/payment/verify', { method: 'POST', body: JSON.stringify(data) })),
  getPaymentStatus: (transactionId) => parseBusinessResponse(api.request(`/payment/status/${transactionId}`)),
  getPaymentHistory: (page = 0, size = 10) => parseBusinessResponse(api.request(`/payment/history?page=${page}&size=${size}`)),

  // Chat APIs
  getUserConversations: async () => {
    return await api.request('/chat/conversations');
  },
  getConversationMessages: async (conversationId, page = 0, size = 50) => {
    return await api.request(`/chat/conversations/${conversationId}/messages?page=${page}&size=${size}`);
  },
  sendMessage: async (data) => {
    return await api.request('/chat/messages', { method: 'POST', body: JSON.stringify(data) });
  },
  createConversation: async (data) => {
    return await api.request('/chat/conversations', { method: 'POST', body: JSON.stringify(data) });
  },
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
    // Trả về mảng để giữ tương thích với chỗ khác
    const res = await api.getProducts(params);
    return res.items ?? [];
};


export default api;