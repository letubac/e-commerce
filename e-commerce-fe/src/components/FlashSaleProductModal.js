/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Search, X, Package, DollarSign, Percent } from 'lucide-react';
import adminApi from '../api/adminApi';
import api from '../api/api';
import toast from '../utils/toast';

function FlashSaleProductModal({ flashSale, isOpen, onClose, onUpdate }) {
  const [products, setProducts] = useState([]);
  const [flashSaleProducts, setFlashSaleProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [selectedProduct, setSelectedProduct] = useState(null);

  const [productForm, setProductForm] = useState({
    productId: null,
    originalPrice: '',
    flashPrice: '',
    discountPercentage: '',
    stockLimit: '',
    maxPerCustomer: 1,
    displayOrder: 0,
    isActive: true
  });

  useEffect(() => {
    if (isOpen && flashSale) {
      fetchFlashSaleProducts();
      fetchAvailableProducts();
    }
  }, [isOpen, flashSale]);

  const fetchFlashSaleProducts = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getFlashSaleProducts(flashSale.id);
      const items = Array.isArray(data) ? data : (data?.content || []);
      setFlashSaleProducts(items);
    } catch (error) {
      console.error('Error fetching flash sale products:', error);
      toast.error('Lỗi khi tải sản phẩm Flash Sale');
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableProducts = async () => {
    try {
      const data = await api.getProducts({ page: 0, size: 100 });
      const items = data?.items || data?.content || [];
      setProducts(items);
    } catch (error) {
      console.error('Error fetching products:', error);
      toast.error('Lỗi khi tải danh sách sản phẩm');
    }
  };

  const handleAddProduct = async (e) => {
    e.preventDefault();

    try {
      // Validation
      if (!selectedProduct) {
        toast.error('Vui lòng chọn sản phẩm');
        return;
      }

      const flashPrice = parseFloat(productForm.flashPrice);
      const originalPrice = parseFloat(productForm.originalPrice || selectedProduct.price);

      if (flashPrice <= 0 || originalPrice <= 0) {
        toast.error('Giá không hợp lệ');
        return;
      }

      if (flashPrice >= originalPrice) {
        toast.error('Giá Flash Sale phải nhỏ hơn giá gốc');
        return;
      }

      const stockLimit = parseInt(productForm.stockLimit);
      if (stockLimit <= 0) {
        toast.error('Số lượng tồn kho phải lớn hơn 0');
        return;
      }

      const requestData = {
        productId: selectedProduct.id,
        originalPrice: originalPrice,
        flashPrice: flashPrice,
        stockLimit: stockLimit,
        stockSold: 0,
        maxPerCustomer: parseInt(productForm.maxPerCustomer) || 1,
        displayOrder: parseInt(productForm.displayOrder) || 0,
        isActive: productForm.isActive
      };

      if (editingProduct) {
        await adminApi.updateFlashSaleProduct(flashSale.id, editingProduct.productId, requestData);
        toast.success('Cập nhật sản phẩm thành công');
      } else {
        await adminApi.addFlashSaleProduct(flashSale.id, requestData);
        toast.success('Thêm sản phẩm vào Flash Sale thành công');
      }

      setShowAddModal(false);
      resetForm();
      fetchFlashSaleProducts();
      if (onUpdate) onUpdate();
    } catch (error) {
      console.error('Error saving flash sale product:', error);
      toast.error(error.message || 'Lỗi khi lưu sản phẩm');
    }
  };

  const handleEditProduct = (flashSaleProduct) => {
    setEditingProduct(flashSaleProduct);
    const product = products.find(p => p.id === flashSaleProduct.productId);
    setSelectedProduct(product);

    setProductForm({
      productId: flashSaleProduct.productId,
      originalPrice: flashSaleProduct.originalPrice,
      flashPrice: flashSaleProduct.flashPrice,
      discountPercentage: calculateDiscount(flashSaleProduct.originalPrice, flashSaleProduct.flashPrice),
      stockLimit: flashSaleProduct.stockLimit,
      maxPerCustomer: flashSaleProduct.maxPerCustomer,
      displayOrder: flashSaleProduct.displayOrder,
      isActive: flashSaleProduct.isActive
    });
    setShowAddModal(true);
  };

  const handleDeleteProduct = async (productId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa sản phẩm khỏi Flash Sale?')) return;

    try {
      await adminApi.removeFlashSaleProduct(flashSale.id, productId);
      toast.success('Xóa sản phẩm thành công');
      fetchFlashSaleProducts();
      if (onUpdate) onUpdate();
    } catch (error) {
      console.error('Error deleting flash sale product:', error);
      toast.error(error.message || 'Lỗi khi xóa sản phẩm');
    }
  };

  const handleProductSelect = (product) => {
    setSelectedProduct(product);
    setProductForm({
      ...productForm,
      productId: product.id,
      originalPrice: product.price,
      flashPrice: '',
      discountPercentage: ''
    });
  };

  const handleFlashPriceChange = (value) => {
    const flashPrice = parseFloat(value);
    const originalPrice = parseFloat(productForm.originalPrice);

    if (!isNaN(flashPrice) && !isNaN(originalPrice) && originalPrice > 0) {
      const discount = ((originalPrice - flashPrice) / originalPrice * 100).toFixed(2);
      setProductForm({
        ...productForm,
        flashPrice: value,
        discountPercentage: discount
      });
    } else {
      setProductForm({
        ...productForm,
        flashPrice: value,
        discountPercentage: ''
      });
    }
  };

  const handleDiscountChange = (value) => {
    const discount = parseFloat(value);
    const originalPrice = parseFloat(productForm.originalPrice);

    if (!isNaN(discount) && !isNaN(originalPrice) && discount >= 0 && discount <= 100) {
      const flashPrice = (originalPrice * (1 - discount / 100)).toFixed(0);
      setProductForm({
        ...productForm,
        discountPercentage: value,
        flashPrice: flashPrice
      });
    } else {
      setProductForm({
        ...productForm,
        discountPercentage: value
      });
    }
  };

  const calculateDiscount = (originalPrice, flashPrice) => {
    if (!originalPrice || !flashPrice) return 0;
    return ((originalPrice - flashPrice) / originalPrice * 100).toFixed(2);
  };

  const resetForm = () => {
    setProductForm({
      productId: null,
      originalPrice: '',
      flashPrice: '',
      discountPercentage: '',
      stockLimit: '',
      maxPerCustomer: 1,
      displayOrder: 0,
      isActive: true
    });
    setSelectedProduct(null);
    setEditingProduct(null);
  };

  const filteredProducts = products.filter(p => 
    p.name.toLowerCase().includes(searchQuery.toLowerCase()) &&
    !flashSaleProducts.some(fsp => fsp.productId === p.id)
  );

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-6xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b border-gray-200">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Quản lý sản phẩm Flash Sale</h2>
            <p className="text-gray-600 mt-1">{flashSale.name}</p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                resetForm();
                setShowAddModal(true);
              }}
              className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            >
              <Plus size={20} />
              Thêm sản phẩm
            </button>
            <button
              onClick={onClose}
              className="text-gray-500 hover:text-gray-700 transition"
            >
              <X size={24} />
            </button>
          </div>
        </div>

        {/* Product List */}
        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
              <p className="mt-4 text-gray-600">Đang tải...</p>
            </div>
          ) : flashSaleProducts.length === 0 ? (
            <div className="text-center py-12">
              <Package size={48} className="mx-auto text-gray-400 mb-4" />
              <p className="text-gray-600 text-lg mb-2">Chưa có sản phẩm nào</p>
              <p className="text-gray-500 mb-4">Thêm sản phẩm vào Flash Sale để bắt đầu</p>
              <button
                onClick={() => setShowAddModal(true)}
                className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
              >
                Thêm sản phẩm đầu tiên
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {flashSaleProducts.map((item) => {
                const product = products.find(p => p.id === item.productId);
                return (
                  <div key={item.id} className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
                    <div className="flex gap-4">
                      <img
                        src={product?.imageUrl || '/images/placeholder.png'}
                        alt={product?.name}
                        className="w-24 h-24 object-cover rounded-lg"
                      />
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2">
                          {product?.name || 'Sản phẩm'}
                        </h3>
                        <div className="space-y-1 text-sm">
                          <div className="flex items-center gap-2">
                            <span className="text-gray-500">Giá gốc:</span>
                            <span className="line-through text-gray-400">
                              {item.originalPrice?.toLocaleString('vi-VN')}₫
                            </span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-gray-500">Giá Flash:</span>
                            <span className="font-bold text-red-600">
                              {item.flashPrice?.toLocaleString('vi-VN')}₫
                            </span>
                            <span className="px-2 py-0.5 bg-red-100 text-red-600 rounded text-xs font-medium">
                              -{calculateDiscount(item.originalPrice, item.flashPrice)}%
                            </span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-gray-500">Kho:</span>
                            <span className="font-medium">{item.stockLimit - (item.stockSold || 0)}/{item.stockLimit}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex justify-end gap-2 mt-3 pt-3 border-t border-gray-100">
                      <button
                        onClick={() => handleEditProduct(item)}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                        title="Sửa"
                      >
                        <Edit size={18} />
                      </button>
                      <button
                        onClick={() => handleDeleteProduct(item.productId)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                        title="Xóa"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Add/Edit Product Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <h3 className="text-xl font-bold mb-4">
                {editingProduct ? 'Sửa sản phẩm Flash Sale' : 'Thêm sản phẩm vào Flash Sale'}
              </h3>

              <form onSubmit={handleAddProduct}>
                {/* Product Selection */}
                {!editingProduct && (
                  <div className="mb-6">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Chọn sản phẩm <span className="text-red-500">*</span>
                    </label>
                    <div className="mb-3">
                      <div className="relative">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                        <input
                          type="text"
                          placeholder="Tìm kiếm sản phẩm..."
                          value={searchQuery}
                          onChange={(e) => setSearchQuery(e.target.value)}
                          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                        />
                      </div>
                    </div>
                    <div className="border border-gray-300 rounded-lg max-h-60 overflow-y-auto">
                      {filteredProducts.length === 0 ? (
                        <p className="text-center py-4 text-gray-500">Không tìm thấy sản phẩm</p>
                      ) : (
                        filteredProducts.map(product => (
                          <button
                            key={product.id}
                            type="button"
                            onClick={() => handleProductSelect(product)}
                            className={`w-full flex items-center gap-3 p-3 hover:bg-gray-50 transition border-b border-gray-100 ${
                              selectedProduct?.id === product.id ? 'bg-red-50' : ''
                            }`}
                          >
                            <img
                              src={product.imageUrl || '/images/placeholder.png'}
                              alt={product.name}
                              className="w-12 h-12 object-cover rounded"
                            />
                            <div className="flex-1 text-left">
                              <p className="font-medium text-gray-900">{product.name}</p>
                              <p className="text-sm text-gray-500">{product.price?.toLocaleString('vi-VN')}₫</p>
                            </div>
                            {selectedProduct?.id === product.id && (
                              <span className="text-red-600 font-medium">✓</span>
                            )}
                          </button>
                        ))
                      )}
                    </div>
                  </div>
                )}

                {selectedProduct && (
                  <>
                    {/* Selected Product Info */}
                    <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                      <div className="flex items-center gap-3">
                        <img
                          src={selectedProduct.imageUrl || '/images/placeholder.png'}
                          alt={selectedProduct.name}
                          className="w-16 h-16 object-cover rounded"
                        />
                        <div>
                          <p className="font-semibold text-gray-900">{selectedProduct.name}</p>
                          <p className="text-sm text-gray-500">Giá hiện tại: {selectedProduct.price?.toLocaleString('vi-VN')}₫</p>
                        </div>
                      </div>
                    </div>

                    {/* Price Settings */}
                    <div className="grid grid-cols-2 gap-4 mb-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Giá gốc <span className="text-red-500">*</span>
                        </label>
                        <div className="relative">
                          <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                          <input
                            type="number"
                            value={productForm.originalPrice}
                            onChange={(e) => setProductForm({ ...productForm, originalPrice: e.target.value })}
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                            required
                          />
                        </div>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Giá Flash Sale <span className="text-red-500">*</span>
                        </label>
                        <div className="relative">
                          <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                          <input
                            type="number"
                            value={productForm.flashPrice}
                            onChange={(e) => handleFlashPriceChange(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                            required
                          />
                        </div>
                      </div>
                    </div>

                    <div className="mb-4">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        % Giảm giá
                      </label>
                      <div className="relative">
                        <Percent className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                        <input
                          type="number"
                          value={productForm.discountPercentage}
                          onChange={(e) => handleDiscountChange(e.target.value)}
                          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                          step="0.01"
                          min="0"
                          max="100"
                        />
                      </div>
                    </div>

                    {/* Stock Settings */}
                    <div className="grid grid-cols-2 gap-4 mb-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Số lượng tồn kho <span className="text-red-500">*</span>
                        </label>
                        <input
                          type="number"
                          value={productForm.stockLimit}
                          onChange={(e) => setProductForm({ ...productForm, stockLimit: e.target.value })}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                          min="1"
                          required
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Số lượng tối đa/khách
                        </label>
                        <input
                          type="number"
                          value={productForm.maxPerCustomer}
                          onChange={(e) => setProductForm({ ...productForm, maxPerCustomer: e.target.value })}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                          min="1"
                        />
                      </div>
                    </div>

                    <div className="mb-4">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Thứ tự hiển thị
                      </label>
                      <input
                        type="number"
                        value={productForm.displayOrder}
                        onChange={(e) => setProductForm({ ...productForm, displayOrder: e.target.value })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                        min="0"
                      />
                    </div>

                    <div className="flex items-center gap-2 mb-6">
                      <input
                        type="checkbox"
                        id="productActive"
                        checked={productForm.isActive}
                        onChange={(e) => setProductForm({ ...productForm, isActive: e.target.checked })}
                        className="w-4 h-4 text-red-600 focus:ring-red-500 border-gray-300 rounded"
                      />
                      <label htmlFor="productActive" className="text-sm text-gray-700">
                        Kích hoạt sản phẩm
                      </label>
                    </div>
                  </>
                )}

                <div className="flex justify-end gap-3">
                  <button
                    type="button"
                    onClick={() => {
                      setShowAddModal(false);
                      resetForm();
                    }}
                    className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                  >
                    Hủy
                  </button>
                  <button
                    type="submit"
                    disabled={!selectedProduct}
                    className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition disabled:bg-gray-300 disabled:cursor-not-allowed"
                  >
                    {editingProduct ? 'Cập nhật' : 'Thêm vào Flash Sale'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default FlashSaleProductModal;
