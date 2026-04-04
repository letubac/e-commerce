/**
 * Response Handler Utility
 * Xử lý BusinessApiResponse từ backend theo cấu trúc mới
 */

import toast from './toast';

/**
 * Cấu trúc BusinessApiResponse từ BE:
 * {
 *   codeStatus: number,      // HTTP status code hoặc custom code
 *   messageStatus: string,   // "SUCCESS" hoặc "ERROR"
 *   description: string,     // Message đã được i18n từ BE
 *   data: any,              // Dữ liệu trả về
 *   took: number,           // Response time (ms)
 *   hiddenDesc: string      // Chi tiết lỗi (optional)
 * }
 */

/**
 * Parse BusinessApiResponse và trả về data
 * @param {Object} response - Response từ BE
 * @returns {any} - Data từ response
 * @throws {Error} - Nếu có lỗi
 */
export const parseBusinessResponse = (response) => {
  // Kiểm tra nếu response có cấu trúc BusinessApiResponse
  if (response && typeof response === 'object') {
    const { codeStatus, messageStatus, description, data } = response;

    // Kiểm tra nếu là success
    if (messageStatus === 'SUCCESS' || (codeStatus >= 200 && codeStatus < 300)) {
      return data;
    }

    // Nếu là error, throw error với message từ BE
    if (messageStatus === 'ERROR' || codeStatus >= 400) {
      const errorMessage = description || 'Có lỗi xảy ra';
      throw new Error(errorMessage);
    }

    // Nếu có data, trả về data (backward compatibility)
    if (data !== undefined && data !== null) {
      return data;
    }
  }

  // Backward compatibility: nếu không có cấu trúc BusinessApiResponse
  return response;
};

/**
 * Parse BusinessApiResponse và hiển thị toast message
 * @param {Object} response - Response từ BE
 * @param {string} successMessage - Message hiển thị khi thành công (optional)
 * @returns {any} - Data từ response
 */
export const parseBusinessResponseWithToast = (response, successMessage = null) => {
  try {
    const data = parseBusinessResponse(response);
    
    // Hiển thị success message
    if (successMessage) {
      toast.success(successMessage);
    } else if (response.description && response.messageStatus === 'SUCCESS') {
      toast.success(response.description);
    }
    
    return data;
  } catch (error) {
    // Toast đã được xử lý trong catch block của component
    throw error;
  }
};

/**
 * Handle error từ API call
 * @param {Error} error - Error object
 * @param {string} defaultMessage - Message mặc định nếu không có message từ error
 */
export const handleApiError = (error, defaultMessage = 'Có lỗi xảy ra') => {
  const errorMessage = error.message || defaultMessage;
  toast.error(errorMessage);
  console.error('API Error:', error);
};

/**
 * Wrapper cho API calls với error handling
 * @param {Function} apiCall - Async function gọi API
 * @param {string} successMessage - Message hiển thị khi thành công (optional)
 * @param {string} errorMessage - Message hiển thị khi lỗi (optional)
 * @returns {Promise<any>} - Data từ response
 */
export const withApiErrorHandling = async (apiCall, successMessage = null, errorMessage = null) => {
  try {
    const response = await apiCall();
    return parseBusinessResponseWithToast(response, successMessage);
  } catch (error) {
    handleApiError(error, errorMessage);
    throw error;
  }
};

const responseHandler = {
  parseBusinessResponse,
  parseBusinessResponseWithToast,
  handleApiError,
  withApiErrorHandling
};

export default responseHandler;
