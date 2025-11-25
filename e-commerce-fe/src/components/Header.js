import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, User, Search, Menu, X, Settings, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import api from '../api/api';

export default function Header() {
  const { user, logout, isAdmin } = useAuth();
  const { cart } = useCart();
  const [searchQuery, setSearchQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (searchQuery.length > 2) fetchSuggestions();
    else setSuggestions([]);
  }, [searchQuery]);

  const fetchSuggestions = async () => {
    try {
      const data = await api.searchSuggestions(searchQuery);
      setSuggestions(data);
    } catch (error) {
      console.error('Error fetching suggestions:', error);
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
          <div className="flex items-center space-x-8">
            <button
              onClick={() => navigate('/')}
              className="text-2xl font-bold text-white flex items-center space-x-2"
            >
              <div className="w-8 h-8 bg-white rounded flex items-center justify-center">
                <span className="text-red-600 font-bold text-sm">T</span>
              </div>
              <span>E-SHOP</span>
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
                <div className="absolute top-full mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-50">
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

              {/* Cart */}
              <button
                onClick={() => navigate('/cart')}
                className="relative p-2 hover:bg-red-700 rounded-full text-white"
              >
                <ShoppingCart size={24} />
                {cart.totalItems > 0 && (
                  <span className="absolute -top-1 -right-1 bg-yellow-400 text-red-800 text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                    {cart.totalItems}
                  </span>
                )}
              </button>

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
