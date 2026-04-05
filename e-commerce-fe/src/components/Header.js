import React, { useEffect, useState, useRef } from 'react';
import { useDebounce } from '../hooks/useDebounce';
import { useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, User, Search, Menu, X, Settings, LogOut, Moon, Sun } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import NotificationBell from './NotificationBell';
import api, { getImageUrl } from '../api/api';
import { useTheme } from '../context/ThemeContext';

export default function Header() {
  const { user, logout, isAdmin } = useAuth();
  const { cart } = useCart();
  const { theme, toggleTheme } = useTheme();
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, 400);
  const [suggestions, setSuggestions] = useState([]);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const suggestionsRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();

  const CART_PLACEHOLDER = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='48' height='48' viewBox='0 0 48 48'%3E%3Crect width='48' height='48' fill='%23f3f4f6'/%3E%3C/svg%3E";

  // Fetch suggestions only after debounce settles (400 ms idle) — avoids per-keystroke API calls
  useEffect(() => {
    if (debouncedSearch.length > 2) {
      fetchSuggestions(debouncedSearch);
    } else {
      setSuggestions([]);
    }
  }, [debouncedSearch]);

  // Close suggestions on outside click
  useEffect(() => {
    const handler = (e) => {
      if (suggestionsRef.current && !suggestionsRef.current.contains(e.target)) {
        setSuggestions([]);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const fetchSuggestions = async (query) => {
    try {
      const data = await api.searchSuggestions(query);
      setSuggestions(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching suggestions:', error);
      setSuggestions([]);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
      setSuggestions([]);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    setSearchQuery(suggestion);
    setSuggestions([]);
    navigate(`/products?search=${encodeURIComponent(suggestion)}`);
  };

  // Kiểm tra route hiện tại để làm highlight menu
  const currentPath = location.pathname;

  return (
    <header className="fixed top-0 left-0 right-0 bg-red-600 shadow-md z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center space-x-4 md:space-x-8">
            <button
              onClick={() => navigate('/')}
              className="text-2xl font-bold text-white flex items-center space-x-2 flex-shrink-0 whitespace-nowrap"
            >
              <div className="w-8 h-8 bg-white rounded flex items-center justify-center flex-shrink-0">
                <span className="text-red-600 font-bold text-sm">T</span>
              </div>
              <span className="whitespace-nowrap">E-SHOP</span>
            </button>



            {/* Search bar */}
            <div className="hidden md:block relative">
              <form onSubmit={handleSearch} className="flex items-center">
                <input
                  type="text"
                  placeholder="Tìm kiếm sản phẩm..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-96 px-4 py-2 border border-gray-300 rounded-l-lg focus:outline-none focus:border-red-500"
                />
                <button 
                  type="submit"
                  className="px-4 py-2 bg-red-700 text-white rounded-r-lg hover:bg-red-800"
                >
                  <Search size={20} />
                </button>
              </form>

              {suggestions.length > 0 && (
                <div ref={suggestionsRef} className="absolute top-full mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                  {suggestions.map((suggestion, index) => (
                    <div
                      key={index}
                      className="px-4 py-2 hover:bg-gray-100 cursor-pointer border-b border-gray-100 last:border-b-0"
                      onClick={() => handleSuggestionClick(suggestion)}
                    >
                      <div className="flex items-center space-x-2">
                        <Search size={14} className="text-gray-400" />
                        <span>{suggestion}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Right side items */}
          <div className="flex items-center space-x-4">
            {/* Build PC Button */}
            {/* <button
              onClick={() => navigate('/products?category=1')}
              className="hidden md:flex items-center space-x-1 px-3 py-2 bg-white text-red-600 rounded-lg hover:bg-gray-100 transition"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              <span className="text-sm font-medium">Build PC</span>
            </button> */}

            {/* Desktop navigation */}
            <nav className="hidden md:flex items-center space-x-6">
              <button
                onClick={() => navigate('/products')}
                className={`hover:text-red-200 ${
                  currentPath === '/products' ? 'text-white font-semibold' : 'text-red-100'
                }`}
              >
                Sản phẩm
              </button>

              {user && (
                <button
                  onClick={() => navigate('/orders')}
                  className={`hover:text-red-200 ${
                    currentPath === '/orders' ? 'text-white font-semibold' : 'text-red-100'
                  }`}
                >
                  Đơn hàng
                </button>
              )}

              {/* Admin menu */}
              {user && isAdmin() && (
                <button
                  onClick={() => navigate('/admin')}
                  className={`hover:text-red-200 ${
                    currentPath.startsWith('/admin') ? 'text-white font-semibold' : 'text-red-100'
                  }`}
                >
                  Quản trị
                </button>
              )}

              {/* Notification Bell - chỉ hiển thị khi đã login */}
              {user && <NotificationBell />}

              {/* Dark/Light mode toggle */}
              <button
                onClick={toggleTheme}
                className="p-2 hover:bg-red-700 rounded-full text-white transition-colors"
                title={theme === 'dark' ? 'Chuyển sang chế độ sáng' : 'Chuyển sang chế độ tối'}
              >
                {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
              </button>

              {/* Cart - chỉ hiển thị cho customer, không hiển thị cho admin */}
              {user && !isAdmin() && (
                <div className="relative group">
                  <button
                    onClick={() => navigate('/cart')}
                    className="relative p-2 hover:bg-red-700 rounded-full text-white"
                  >
                    <ShoppingCart size={24} />
                    {(cart.itemCount || 0) > 0 && (
                      <span className="absolute top-0 right-0 -mt-1 -mr-1 bg-yellow-400 text-red-800 text-xs rounded-full h-5 w-5 flex items-center justify-center font-bold">
                        {cart.itemCount || 0}
                      </span>
                    )}
                  </button>
                  {/* Cart hover popup */}
                  <div className="absolute right-0 top-full mt-1 w-80 bg-white border border-gray-100 rounded-lg shadow-xl z-50 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150">
                    <div className="px-4 py-2.5 border-b border-gray-200">
                      <p className="text-sm font-semibold text-gray-800">Sản phẩm mới thêm</p>
                    </div>
                    {(cart.items || []).length === 0 ? (
                      <div className="py-8 text-center text-gray-500 text-sm">Giỏ hàng trống</div>
                    ) : (
                      <>
                        <div className="max-h-60 overflow-y-auto">
                          {(cart.items || []).slice(0, 5).map((item, idx) => (
                            <div key={idx} className="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 border-b border-gray-100 last:border-0">
                              <img
                                src={getImageUrl(item.productImage) || CART_PLACEHOLDER}
                                alt={item.productName}
                                className="w-12 h-12 object-cover rounded border border-gray-200 flex-shrink-0"
                                onError={(e) => { e.target.onerror = null; e.target.src = CART_PLACEHOLDER; }}
                              />
                              <div className="flex-1 min-w-0">
                                <p className="text-xs text-gray-800 font-medium line-clamp-2">{item.productName}</p>
                                <p className="text-xs text-red-600 mt-0.5">
                                  x{item.quantity} — {((item.price || item.unitPrice || 0) * item.quantity).toLocaleString('vi-VN')}₫
                                </p>
                              </div>
                            </div>
                          ))}
                        </div>
                        <div className="px-4 py-3 border-t border-gray-200">
                          <div className="flex justify-between text-sm mb-2">
                            <span className="text-gray-500">{cart.itemCount || 0} sản phẩm trong giỏ</span>
                            <span className="font-semibold text-red-600">{(cart.totalPrice || 0).toLocaleString('vi-VN')}₫</span>
                          </div>
                          <button
                            onClick={() => navigate('/cart')}
                            className="w-full py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition"
                          >
                            Xem giỏ hàng
                          </button>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              )}
              
              {/* Cart cho guest user (chưa login) */}
              {!user && (
                <div className="relative group">
                  <button
                    onClick={() => navigate('/cart')}
                    className="relative p-2 hover:bg-red-700 rounded-full text-white"
                  >
                    <ShoppingCart size={24} />
                  </button>
                  <div className="absolute right-0 top-full mt-1 w-64 bg-white border border-gray-100 rounded-lg shadow-xl z-50 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150">
                    <div className="py-6 px-4 text-center">
                      <ShoppingCart size={32} className="mx-auto text-gray-300 mb-2" />
                      <p className="text-sm text-gray-500 mb-3">Vui lòng đăng nhập để xem giỏ hàng</p>
                      <button
                        onClick={() => navigate('/login')}
                        className="w-full py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition"
                      >
                        Đăng nhập
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* User menu */}
              {user ? (
                <div className="relative group">
                  <button className="flex items-center space-x-2 p-2 hover:bg-red-700 rounded-lg text-white">
                    <User size={24} />
                    <span>{user.fullName}</span>
                  </button>
                  <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all">
                    <button 
                      onClick={() => navigate('/profile')}
                      className="w-full text-left px-4 py-2 hover:bg-gray-100 flex items-center space-x-2 text-gray-700"
                    >
                      <User size={18} />
                      <span>Tài khoản của tôi</span>
                    </button>
                    <button className="w-full text-left px-4 py-2 hover:bg-gray-100 flex items-center space-x-2 text-gray-700">
                      <Settings size={18} />
                      <span>Cài đặt</span>
                    </button>
                    <button
                      onClick={logout}
                      className="w-full text-left px-4 py-2 hover:bg-gray-100 flex items-center space-x-2 text-red-600"
                    >
                      <LogOut size={18} />
                      <span>Đăng xuất</span>
                    </button>
                  </div>
                </div>
              ) : (
                <div className="flex items-center space-x-2 text-white">
                  <button
                    onClick={() => navigate('/login')}
                    className="px-4 py-2 hover:bg-red-700 rounded-lg"
                  >
                    Đăng nhập
                  </button>
                  <span>/</span>
                  <button
                    onClick={() => navigate('/register')}
                    className="px-4 py-2 hover:bg-red-700 rounded-lg"
                  >
                    Đăng ký
                  </button>
                </div>
              )}
            </nav>

            {/* Mobile menu button */}
            <button
              onClick={() => setShowMobileMenu(!showMobileMenu)}
              className="md:hidden p-2 text-white"
            >
              {showMobileMenu ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {showMobileMenu && (
        <div className="md:hidden border-t border-red-500 bg-red-600">
          <div className="px-4 py-4">
            {/* Mobile search */}
            <form onSubmit={handleSearch} className="flex items-center mb-4">
              <input
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-l-lg focus:outline-none focus:border-red-500"
              />
              <button 
                type="submit"
                className="px-3 py-2 bg-red-700 text-white rounded-r-lg"
              >
                <Search size={18} />
              </button>
            </form>

            {/* Mobile navigation */}
            <nav className="space-y-2">
              <button
                onClick={() => {
                  navigate('/products');
                  setShowMobileMenu(false);
                }}
                className="block w-full text-left px-4 py-2 text-white hover:bg-red-700 rounded"
              >
                Sản phẩm
              </button>

              <button
                onClick={() => {
                  navigate('/products?category=1');
                  setShowMobileMenu(false);
                }}
                className="block w-full text-left px-4 py-2 text-white hover:bg-red-700 rounded"
              >
                Build PC
              </button>

              {user && (
                <button
                  onClick={() => {
                    navigate('/orders');
                    setShowMobileMenu(false);
                  }}
                  className="block w-full text-left px-4 py-2 text-white hover:bg-red-700 rounded"
                >
                  Đơn hàng
                </button>
              )}

              {/* Admin menu for mobile */}
              {user && isAdmin() && (
                <button
                  onClick={() => {
                    navigate('/admin');
                    setShowMobileMenu(false);
                  }}
                  className="block w-full text-left px-4 py-2 text-white hover:bg-red-700 rounded"
                >
                  Quản trị
                </button>
              )}

              {!user && (
                <>
                  <button
                    onClick={() => {
                      navigate('/login');
                      setShowMobileMenu(false);
                    }}
                    className="block w-full text-left px-4 py-2 text-white hover:bg-red-700 rounded"
                  >
                    Đăng nhập
                  </button>
                  <button
                    onClick={() => {
                      navigate('/register');
                      setShowMobileMenu(false);
                    }}
                    className="block w-full text-left px-4 py-2 text-white hover:bg-red-700 rounded"
                  >
                    Đăng ký
                  </button>
                </>
              )}
            </nav>
          </div>
        </div>
      )}
    </header>
  );
}
