import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Filter, Search } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import CategoryBar from '../components/CategoryBar';
import api from '../api/api';

function ProductsPage() {

  // Xử lý thay đổi filter
  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setCurrentPage(0);
  };

  // Xử lý thay đổi sort
  const handleSortChange = (newSort) => {
    setSortBy(newSort);
    setCurrentPage(0);
  };

  // Xử lý xem chi tiết sản phẩm
  const handleViewProductDetails = (productId) => {
    navigate(`/product/${productId}`);
  };
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    category: searchParams.get('category') || '',
    brand: searchParams.get('brand') || '',
    categoryId: searchParams.get('categoryId') ? parseInt(searchParams.get('categoryId')) : null,
    brandId: searchParams.get('brandId') ? parseInt(searchParams.get('brandId')) : null,
    minPrice: '',
    maxPrice: '',
    search: searchParams.get('search') || ''
  });
  const [sortBy, setSortBy] = useState('newest');
  const [showFilters, setShowFilters] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page: currentPage,
        size: 12,
        ...filters
      };

      if (sortBy.includes('-')) {
        const [field, direction] = sortBy.split('-');
        params.sortBy = field;
        params.sortDirection = direction === 'asc' ? 'asc' : 'desc';
      } else {
        params.sortBy = 'createdAt';
        params.sortDirection = 'desc';
      }

      const response = await api.getProducts(params);
      console.log('API Response:', response);
      console.log('Products:', response.items);
      setProducts(response.items || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  }, [currentPage, filters, sortBy]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    if (category) {
      // Send categoryId to backend instead of category name
      setFilters(prev => ({ 
        ...prev, 
        categoryId: category.id,
        category: '',
        brand: '',
        brandId: null
      }));
    } else {
      setFilters(prev => ({ 
        ...prev, 
        categoryId: null,
        category: ''
      }));
    }
    setCurrentPage(0);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Category Bar - Shopee Style */}
      <CategoryBar 
        onCategorySelect={handleCategorySelect}
        selectedCategory={selectedCategory}
      />

      <div className="container mx-auto px-4 py-8">
        <div className="flex-1">
          <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
              <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                  <h1 className="text-2xl font-bold text-gray-800 mb-2">Sản phẩm</h1>
                  <p className="text-gray-600">
                    {loading ? 'Đang tải...' : `${products.length} sản phẩm`}
                  </p>
                </div>

                <div className="flex flex-col md:flex-row gap-4">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="text"
                      placeholder="Tìm kiếm sản phẩm..."
                      value={filters.search}
                      onChange={(e) => handleFilterChange('search', e.target.value)}
                      className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    />
                  </div>

                  <select
                    value={sortBy}
                    onChange={(e) => handleSortChange(e.target.value)}
                    className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  >
                    <option value="newest">Mới nhất</option>
                    <option value="price-asc">Giá tăng dần</option>
                    <option value="price-desc">Giá giảm dần</option>
                    <option value="name">Tên A-Z</option>
                  </select>

                  <button
                    onClick={() => setShowFilters(!showFilters)}
                    className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                  >
                    <Filter size={20} />
                    Bộ lọc
                  </button>
                </div>
              </div>

              {showFilters && (
                <div className="mt-6 pt-6 border-t border-gray-200">
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Danh mục
                      </label>
                      <input
                        type="text"
                        placeholder="Danh mục"
                        value={filters.category}
                        onChange={(e) => handleFilterChange('category', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Thương hiệu
                      </label>
                      <input
                        type="text"
                        placeholder="Thương hiệu"
                        value={filters.brand}
                        onChange={(e) => handleFilterChange('brand', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Giá từ
                      </label>
                      <input
                        type="number"
                        placeholder="0"
                        value={filters.minPrice}
                        onChange={(e) => handleFilterChange('minPrice', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Giá đến
                      </label>
                      <input
                        type="number"
                        placeholder="999999999"
                        value={filters.maxPrice}
                        onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      />
                    </div>
                  </div>
                </div>
              )}
            </div>

            {loading ? (
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                {[...Array(8)].map((_, index) => (
                  <div key={index} className="bg-white rounded-lg shadow-sm p-4 animate-pulse">
                    <div className="bg-gray-300 h-48 rounded-lg mb-4"></div>
                    <div className="bg-gray-300 h-4 rounded mb-2"></div>
                    <div className="bg-gray-300 h-4 rounded w-2/3 mb-2"></div>
                    <div className="bg-gray-300 h-6 rounded w-1/2"></div>
                  </div>
                ))}
              </div>
            ) : products.length > 0 ? (
              <>
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                  {products.map((product) => (
                    <ProductCard
                      key={product.id}
                      product={product}
                      onViewDetails={handleViewProductDetails}
                    />
                  ))}
                </div>

                {totalPages > 1 && (
                  <div className="flex justify-center mt-8">
                    <nav className="flex gap-2">
                      <button
                        onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                        disabled={currentPage === 0}
                        className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                      >
                        Trước
                      </button>
                      
                      {[...Array(Math.min(5, totalPages))].map((_, index) => {
                        const pageNumber = currentPage < 3 ? index : currentPage - 2 + index;
                        if (pageNumber >= totalPages) return null;
                        
                        return (
                          <button
                            key={pageNumber}
                            onClick={() => setCurrentPage(pageNumber)}
                            className={`px-4 py-2 border rounded-lg ${
                              currentPage === pageNumber
                                ? 'bg-red-600 text-white border-red-600'
                                : 'border-gray-300 hover:bg-gray-50'
                            }`}
                          >
                            {pageNumber + 1}
                          </button>
                        );
                      })}
                      
                      <button
                        onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                        disabled={currentPage === totalPages - 1}
                        className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                      >
                        Sau
                      </button>
                    </nav>
                  </div>
                )}
              </>
            ) : (
              <div className="text-center py-12">
                <div className="text-gray-500 text-lg mb-4">Không tìm thấy sản phẩm nào</div>
                <button
                  onClick={() => {
                    setFilters({
                      category: '',
                      brand: '',
                      minPrice: '',
                      maxPrice: '',
                      search: ''
                    });
                    setCurrentPage(0);
                  }}
                  className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                >
                  Xóa bộ lọc
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
  );
}

export default ProductsPage;
