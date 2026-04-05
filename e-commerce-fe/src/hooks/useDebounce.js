/**
 * author: LeTuBac
 */
import { useState, useEffect } from 'react';

/**
 * Debounces a value by the specified delay.
 * Returns the debounced value that only updates after the user has
 * stopped changing the input for `delay` milliseconds.
 *
 * @param {any} value - The value to debounce
 * @param {number} delay - Delay in milliseconds (default: 400)
 * @returns {any} Debounced value
 */
export function useDebounce(value, delay = 400) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}

export default useDebounce;
