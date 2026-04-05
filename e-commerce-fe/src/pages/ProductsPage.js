import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Filter, Search, X, ChevronDown, Grid, List } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import CategoryBar from '../components/CategoryBar';
import FlashSale from '../components/FlashSale';
import api from '../api/api';

function ProductsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(parseInt(searchParams.get('page') || '0'));

  const [filters, setFilters] = useState({
    category: searchParams.get('category') || '',
    brand: searchParams.get('brand') || '',
    categoryId: searchParams.get('categoryId') ? parseInt(searchParams.get('categoryId')) : null,
    brandId: searchParams.get('brandId') ? parseInt(searchParams.get('brandId')) : null,
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    search: searchParams.get('search') || ''
  });

  // Separate state for raw search input to support debounce
  const [searchInput, setSearchInput] = useState(searchParams.get('search') || '');
  const searchDebounceRef = useRef(null);

  const [sortBy, setSortBy] = useState(searchParams.get('sort') || 'newest');
  const [showFilters, setShowFilters] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  // View mode: 'pagination' (default) or 'infinite' (Load More)
  const [viewMode, setViewMode] = useState('pagination');
  const [loadingMore, setLoadingMore] = useState(false);
  const [accumulatedProducts, setAccumulatedProducts] = useState([]);

  // Debounced search: update filters.search after 450ms of no typing
  const handleSearchInputChange = (e) => {
    const val = e.target.value;
    setSearchInput(val);
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    searchDebounceRef.current = setTimeout(() => {
      setFilters(prev => ({ ...prev, search: val }));
      setCurrentPage(0);
      setAccumulatedProducts([]);
    }, 450);
  };

  // Xử lý thay đổi filter
  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setCurrentPage(0);
    setAccumulatedProducts([]);
  };

  // Xử lý thay đổi sort
  const handleSortChange = (newSort) => {
    setSortBy(newSort);
    setCurrentPage(0);
    setAccumulatedProducts([]);
  };

  // Xử lý xem chi tiết sản phẩm
  const handleViewProductDetails = (productId) => {
    navigate(`/product/${productId}`);
  };

  const fetchProducts = useCallback(async (append = false) => {
    if (append) {
      setLoadingMore(true);
    } else {
      setLoading(true);
    }
    try {
      const params = {
        page: currentPage,
        size: 12,
      };

      // Apply filters — only include non-empty values
      if (filters.categoryId) params.categoryId = filters.categoryId;
      else if (filters.category) params.category = filters.category;
      if (filters.brandId) params.brandId = filters.brandId;
      else if (filters.brand) params.brand = filters.brand;
      if (filters.minPrice) params.minPrice = filters.minPrice;
      if (filters.maxPrice) params.maxPrice = filters.maxPrice;
      if (filters.search) params.search = filters.search;

      // Sort handling
      if (sortBy === 'price-asc') {
        params.sortBy = 'price'; params.sortDirection = 'asc';
      } else if (sortBy === 'price-desc') {
        params.sortBy = 'price'; params.sortDirection = 'desc';
      } else if (sortBy === 'name-asc') {
        params.sortBy = 'name'; params.sortDirection = 'asc';
      } else {
        // Default: newest
        params.sortBy = 'createdAt'; params.sortDirection = 'desc';
      }

      const response = await api.getProducts(params);
      const newItems = response.items || [];

      if (append) {
        setAccumulatedProducts(prev => [...prev, ...newItems]);
      } else {
        setProducts(newItems);
        // Reset accumulated list when not appending (new search/filter)
        setAccumulatedProducts(newItems);
      }

      // Sanity-check totalPages: cap at a reasonable maximum (500) to avoid display bugs
      const rawPages = response.totalPages ?? 0;
      const sanitizedPages = (typeof rawPages === 'number' && rawPages > 0 && rawPages <= 50000)
        ? rawPages
        : 0;
      setTotalPages(sanitizedPages);
      setTotalElements(response.totalElements ?? 0);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [currentPage, filters, sortBy]);

  const prevPageRef = useRef(currentPage);
  useEffect(() => {
    const isLoadMore = viewMode === 'infinite' && currentPage > 0 && currentPage !== prevPageRef.current;
    prevPageRef.current = currentPage;
    fetchProducts(isLoadMore);
  }, [fetchProducts]); // eslint-disable-line react-hooks/exhaustive-deps

  // Sync URL search params
  useEffect(() => {
    const params = {};
    if (filters.search) params.search = filters.search;
    if (filters.categoryId) params.categoryId = String(filters.categoryId);
    if (filters.category) params.category = filters.category;
    if (filters.minPrice) params.minPrice = filters.minPrice;
    if (filters.maxPrice) params.maxPrice = filters.maxPrice;
    if (sortBy !== 'newest') params.sort = sortBy;
    if (currentPage > 0) params.page = String(currentPage);
    setSearchParams(params, { replace: true });
  }, [filters, sortBy, currentPage, setSearchParams]);

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    if (category) {
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
    setAccumulatedProducts([]);
  };

  // Pagination: show at most 7 page numbers centered around currentPage
  const getPaginationRange = () => {
    const delta = 3;
    const range = [];
    const left = Math.max(0, currentPage - delta);
    const right = Math.min(totalPages - 1, currentPage + delta);
    for (let i = left; i <= right; i++) range.push(i);
    return range;
  };

  const clearAllFilters = () => {
    setFilters({ category: '', brand: '', categoryId: null, brandId: null, minPrice: '', maxPrice: '', search: '' });
    setSearchInput('');
    setSortBy('newest');
    setSelectedCategory(null);
    setCurrentPage(0);
    setAccumulatedProducts([]);
  };

  const hasActiveFilters = filters.search || filters.categoryId || filters.category || filters.minPrice || filters.maxPrice || filters.brand;

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Category Bar - Shopee Style */}
      <CategoryBar
        onCategorySelect={handleCategorySelect}
        selectedCategory={selectedCategory}
      />

      {/* Flash Sale Banner */}
      <div className="max-w-7xl mx-auto px-4 pt-4">
        <FlashSale />
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="flex-1">
          <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div>
                <h1 className="text-2xl font-bold text-gray-800 mb-1">Sản phẩm</h1>
                <p className="text-gray-500 text-sm">
                  {loading ? 'Đang tải...' : `${totalElements > 0 ? totalElements.toLocaleString('vi-VN') : products.length} sản phẩm`}
                </p>
              </div>

              <div className="flex flex-col md:flex-row gap-3 w-full md:w-auto">
                {/* Search with clear button */}
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                  <input
                    type="text"
                    placeholder="Tìm kiếm sản phẩm..."
                    value={searchInput}
                    onChange={handleSearchInputChange}
                    className="pl-10 pr-8 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent w-full md:w-64"
                  />
                  {searchInput && (
                    <button
                      onClick={() => { setSearchInput(''); setFilters(prev => ({ ...prev, search: '' })); setCurrentPage(0); }}
                      className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      <X size={16} />
                    </button>
                  )}
                </div>

                <select
                  value={sortBy}
                  onChange={(e) => handleSortChange(e.target.value)}
                  className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                >
                  <option value="newest">Mới nhất</option>
                  <option value="price-asc">Giá tăng dần</option>
                  <option value="price-desc">Giá giảm dần</option>
                  <option value="name-asc">Tên A-Z</option>
                </select>

                <button
                  onClick={() => setShowFilters(!showFilters)}
                  className={`flex items-center gap-2 px-4 py-2 border rounded-lg transition ${showFilters ? 'bg-red-50 border-red-300 text-red-600' : 'border-gray-300 hover:bg-gray-50'}`}
                >
                  <Filter size={18} />
                  Bộ lọc {hasActiveFilters && <span className="bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">!</span>}
                </button>

                {/* View mode toggle: Pagination ↔ Infinite Scroll */}
                <button
                  onClick={() => {
                    const next = viewMode === 'pagination' ? 'infinite' : 'pagination';
                    setViewMode(next);
                    setCurrentPage(0);
                    setAccumulatedProducts([]);
                  }}
                  title={viewMode === 'pagination' ? 'Chuyển sang cuộn vô hạn' : 'Chuyển về phân trang'}
                  className="flex items-center gap-1.5 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition text-sm text-gray-600"
                >
                  {viewMode === 'pagination' ? <List size={16} /> : <Grid size={16} />}
                  {viewMode === 'pagination' ? 'Load More' : 'Phân trang'}
                </button>
              </div>
            </div>

            {/* Active filter tags */}
            {hasActiveFilters && (
              <div className="flex flex-wrap gap-2 mt-3 pt-3 border-t border-gray-100">
                {filters.search && (
                  <span className="inline-flex items-center gap-1 bg-red-50 text-red-700 text-xs px-2.5 py-1 rounded-full">
                    Tìm: {filters.search}
                    <button onClick={() => { setFilters(p => ({ ...p, search: '' })); setSearchInput(''); }}><X size={12} /></button>
                  </span>
                )}
                {(filters.categoryId || filters.category) && (
                  <span className="inline-flex items-center gap-1 bg-blue-50 text-blue-700 text-xs px-2.5 py-1 rounded-full">
                    Danh mục đã chọn
                    <button onClick={() => { setFilters(p => ({ ...p, categoryId: null, category: '' })); setSelectedCategory(null); }}><X size={12} /></button>
                  </span>
                )}
                {filters.minPrice && (
                  <span className="inline-flex items-center gap-1 bg-green-50 text-green-700 text-xs px-2.5 py-1 rounded-full">
                    Từ {Number(filters.minPrice).toLocaleString('vi-VN')}₫
                    <button onClick={() => setFilters(p => ({ ...p, minPrice: '' }))}><X size={12} /></button>
                  </span>
                )}
                {filters.maxPrice && (
                  <span className="inline-flex items-center gap-1 bg-green-50 text-green-700 text-xs px-2.5 py-1 rounded-full">
                    Đến {Number(filters.maxPrice).toLocaleString('vi-VN')}₫
                    <button onClick={() => setFilters(p => ({ ...p, maxPrice: '' }))}><X size={12} /></button>
                  </span>
                )}
                <button onClick={clearAllFilters} className="text-xs text-gray-500 underline hover:text-red-600">Xóa tất cả</button>
              </div>
            )}

            {showFilters && (
              <div className="mt-4 pt-4 border-t border-gray-200">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Thương hiệu</label>
                    <input
                      type="text"
                      placeholder="Nhập thương hiệu"
                      value={filters.brand}
                      onChange={(e) => handleFilterChange('brand', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Giá từ (₫)</label>
                    <input
                      type="number"
                      placeholder="0"
                      value={filters.minPrice}
                      onChange={(e) => handleFilterChange('minPrice', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Giá đến (₫)</label>
                    <input
                      type="number"
                      placeholder="Không giới hạn"
                      value={filters.maxPrice}
                      onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent text-sm"
                    />
                  </div>
                  <div className="flex items-end">
                    <button
                      onClick={clearAllFilters}
                      className="w-full px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition text-sm"
                    >
                      Xóa bộ lọc
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>

          {loading ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
              {[...Array(10)].map((_, index) => (
                <div key={index} className="bg-white rounded-lg shadow-sm p-4 animate-pulse">
                  <div className="bg-gray-200 h-48 rounded-lg mb-4"></div>
                  <div className="bg-gray-200 h-4 rounded mb-2"></div>
                  <div className="bg-gray-200 h-4 rounded w-2/3 mb-2"></div>
                  <div className="bg-gray-200 h-6 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : (viewMode === 'infinite' ? accumulatedProducts : products).length > 0 ? (
            <>
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                {(viewMode === 'infinite' ? accumulatedProducts : products).map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onViewDetails={handleViewProductDetails}
                  />
                ))}
              </div>

              {/* Pagination mode */}
              {viewMode === 'pagination' && totalPages > 1 && (
                <div className="flex justify-center items-center mt-8 gap-1">
                  <button
                    onClick={() => setCurrentPage(0)}
                    disabled={currentPage === 0}
                    className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 text-sm"
                  >
                    «
                  </button>
                  <button
                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                    disabled={currentPage === 0}
                    className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 text-sm"
                  >
                    ‹
                  </button>

                  {getPaginationRange().map((pageNumber) => (
                    <button
                      key={pageNumber}
                      onClick={() => setCurrentPage(pageNumber)}
                      className={`px-3 py-2 border rounded-lg text-sm min-w-[40px] ${
                        currentPage === pageNumber
                          ? 'bg-red-600 text-white border-red-600 font-semibold'
                          : 'border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {pageNumber + 1}
                    </button>
                  ))}

                  <button
                    onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                    disabled={currentPage >= totalPages - 1}
                    className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 text-sm"
                  >
                    ›
                  </button>
                  <button
                    onClick={() => setCurrentPage(totalPages - 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="px-3 py-2 border border-gray-300 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 text-sm"
                  >
                    »
                  </button>

                  <span className="text-sm text-gray-500 ml-2">
                    Trang {currentPage + 1} / {totalPages}
                  </span>
                </div>
              )}

              {/* Infinite scroll mode — Load More button */}
              {viewMode === 'infinite' && currentPage < totalPages - 1 && (
                <div className="flex justify-center mt-8">
                  <button
                    onClick={() => setCurrentPage(prev => prev + 1)}
                    disabled={loadingMore}
                    className="flex items-center gap-2 px-8 py-3 bg-red-600 text-white rounded-full hover:bg-red-700 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
                  >
                    {loadingMore ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        Đang tải...
                      </>
                    ) : (
                      <>
                        <ChevronDown size={18} />
                        Tải thêm sản phẩm
                      </>
                    )}
                  </button>
                </div>
              )}
              {viewMode === 'infinite' && currentPage >= totalPages - 1 && accumulatedProducts.length > 0 && (
                <p className="text-center text-sm text-gray-400 mt-8 py-2">
                  Đã hiển thị tất cả {accumulatedProducts.length} sản phẩm
                </p>
              )}
            </>
          ) : (
            <div className="text-center py-16 bg-white rounded-lg shadow-sm">
              <Search size={48} className="mx-auto text-gray-300 mb-4" />
              <p className="text-gray-500 text-lg mb-2">Không tìm thấy sản phẩm nào</p>
              <p className="text-gray-400 text-sm mb-6">Thử thay đổi từ khoá tìm kiếm hoặc bộ lọc</p>
              <button
                onClick={clearAllFilters}
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
