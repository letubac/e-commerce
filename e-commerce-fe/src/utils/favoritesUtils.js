/**
 * localStorage-based favorites/wishlist utility.
 * Stores product summaries for display in the Profile favorites tab.
 * Key: 'eshop_favorites'
 * Value: array of { id, name, price, imageUrl, slug }
 */

const STORAGE_KEY = 'eshop_favorites';

export const getFavorites = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
};

export const isFavorite = (productId) => {
  return getFavorites().some(f => f.id === productId);
};

export const addFavorite = (product) => {
  const list = getFavorites();
  if (!list.some(f => f.id === product.id)) {
    list.unshift({
      id: product.id,
      name: product.name,
      price: product.salePrice || product.price || 0,
      imageUrl: product.imageUrl || product.image || null,
    });
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  }
};

export const removeFavorite = (productId) => {
  const list = getFavorites().filter(f => f.id !== productId);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
};

export const toggleFavorite = (product) => {
  if (isFavorite(product.id)) {
    removeFavorite(product.id);
    return false;
  } else {
    addFavorite(product);
    return true;
  }
};
