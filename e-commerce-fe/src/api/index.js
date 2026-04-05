/**
 * author: LeTuBac
 */
export { API_BASE_URL } from './api';

export const api = {
  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Request failed');
    }

    return response.json();
  },

  // API xác thực email
  verifyEmail(token) {
    return this.request(`/auth/verify-email?token=${token}`, { method: 'GET' });
  },

  // API gửi lại email xác thực
  resendVerification(email) {
    return this.request('/auth/resend-verification', {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },
};
