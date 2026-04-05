/**
 * author: LeTuBac
 */
import toast from './toast';

/**
 * Extracts a human-readable error message from a caught API error.
 * BE returns `error.message` = the `description` field from BusinessApiResponse
 * (already parsed by api.request → parseBusinessResponse).
 *
 * @param {Error|any} error - The caught error object
 * @param {string} fallbackMsg - Fallback message if none found
 * @returns {string} The error message string
 */
export function getErrorMessage(error, fallbackMsg = 'Đã xảy ra lỗi. Vui lòng thử lại.') {
  if (!error) return fallbackMsg;
  return error?.message || error?.description || fallbackMsg;
}

/**
 * Shows a toast error for an API error.
 * Silently skips authentication errors (handled globally).
 *
 * @param {Error|any} error - The caught error object
 * @param {string} fallbackMsg - Fallback message shown in toast
 * @returns {string} The message that was shown (or skipped)
 */
export function handleApiError(error, fallbackMsg = 'Đã xảy ra lỗi. Vui lòng thử lại.') {
  const message = getErrorMessage(error, fallbackMsg);
  // Skip auth errors — they are handled globally by the AuthContext/api interceptor
  if (message === 'Authentication required' || message === 'Unauthorized') {
    return message;
  }
  toast.error(message);
  return message;
}

export default handleApiError;
