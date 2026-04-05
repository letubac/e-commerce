/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import {
  X, Calendar, Clock, ChevronRight, ChevronLeft, Plus, Trash2,
  Search, Package, Percent, DollarSign, Check, AlertCircle, Zap
} from 'lucide-react';
import adminApi from '../api/adminApi';
import api from '../api/api';
import toast from '../utils/toast';

const STEP_SCHEDULE = 1;
const STEP_PRODUCTS = 2;
const STEP_CONFIRM = 3;

function FlashSaleWizardModal({ isOpen, onClose, onDone }) {
  const [step, setStep] = useState(STEP_SCHEDULE);

  // Step 1 state
  const [scheduleForm, setScheduleForm] = useState({
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    backgroundColor: '#EF4444',
    bannerImageUrl: '',
    isActive: false
  });
  const [scheduleErrors, setScheduleErrors] = useState({});

  // Step 2 state
  const [allProducts, setAllProducts] = useState([]);
  const [totalProductPages, setTotalProductPages] = useState(1);
  const [productPage, setProductPage] = useState(0);
  const PRODUCT_PAGE_SIZE = 10;
  const [productsLoading, setProductsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedItems, setSelectedItems] = useState([]); // [{productId, name, imageUrl, originalPrice, flashPrice, discountPct, stockLimit, maxPerCustomer, displayOrder}]

  // Step 3 state
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen && step === STEP_PRODUCTS) {
      setProductPage(0);
      fetchProducts(0);
    }
  }, [isOpen, step]);

  useEffect(() => {
    if (step === STEP_PRODUCTS) {
      fetchProducts(productPage);
    }
  }, [productPage]);

  // Re-fetch when search query changes (debounced via separate effect)
  useEffect(() => {
    if (step !== STEP_PRODUCTS) return;
    const timer = setTimeout(() => {
      setProductPage(0);
      fetchProducts(0);
    }, 350);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  const fetchProducts = async (page = 0) => {
    try {
      setProductsLoading(true);
      const params = { page, size: PRODUCT_PAGE_SIZE, isActive: true };
      if (searchQuery.trim()) params.keyword = searchQuery.trim();
      const data = await api.getProducts(params);
      const items = (data?.items || data?.content || []).filter(p => p.stockQuantity > 0 || p.quantity > 0);
      setAllProducts(items);
      setTotalProductPages(data?.totalPages || 1);
    } catch {
      toast.error('Lỗi khi tải sản phẩm');
    } finally {
      setProductsLoading(false);
    }
  };

  const validateStep1 = () => {
    const errors = {};
    if (!scheduleForm.name.trim()) errors.name = 'Vui lòng nhập tên chương trình';
    if (!scheduleForm.startTime) errors.startTime = 'Vui lòng chọn thời gian bắt đầu';
    if (!scheduleForm.endTime) errors.endTime = 'Vui lòng chọn thời gian kết thúc';

    if (scheduleForm.startTime && scheduleForm.endTime) {
      const start = new Date(scheduleForm.startTime);
      const end = new Date(scheduleForm.endTime);
      if (end <= start) errors.endTime = 'Thời gian kết thúc phải sau thời gian bắt đầu';
      if (start < new Date()) errors.startTime = 'Thời gian bắt đầu phải trong tương lai';
    }

    setScheduleErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleNext = () => {
    if (step === STEP_SCHEDULE) {
      if (validateStep1()) setStep(STEP_PRODUCTS);
    } else if (step === STEP_PRODUCTS) {
      if (selectedItems.length === 0) {
        toast.error('Vui lòng thêm ít nhất một sản phẩm vào Flash Sale');
        return;
      }
      const invalid = selectedItems.find(item => !item.flashPrice || parseFloat(item.flashPrice) <= 0 || parseFloat(item.flashPrice) >= parseFloat(item.originalPrice));
      if (invalid) {
        toast.error(`Giá Flash Sale cho "${invalid.name}" không hợp lệ (phải > 0 và < giá gốc)`);
        return;
      }
      const noStock = selectedItems.find(item => !item.stockLimit || parseInt(item.stockLimit) <= 0);
      if (noStock) {
        toast.error(`Vui lòng nhập số lượng tồn kho cho "${noStock.name}"`);
        return;
      }
      setStep(STEP_CONFIRM);
    }
  };

  const handleBack = () => {
    if (step > STEP_SCHEDULE) setStep(s => s - 1);
  };

  const addProduct = (product) => {
    if (selectedItems.find(it => it.productId === product.id)) {
      toast.error('Sản phẩm đã được chọn');
      return;
    }
    setSelectedItems(prev => [
      ...prev,
      {
        productId: product.id,
        name: product.name,
        imageUrl: product.productImages?.[0]?.imageUrl,
        originalPrice: String(product.price || ''),
        flashPrice: '',
        discountPct: '',
        stockLimit: '',
        maxPerCustomer: 1,
        displayOrder: prev.length
      }
    ]);
  };

  const removeProduct = (productId) => {
    setSelectedItems(prev => prev.filter(it => it.productId !== productId));
  };

  const updateItem = (productId, field, value) => {
    setSelectedItems(prev => prev.map(it => {
      if (it.productId !== productId) return it;
      const updated = { ...it, [field]: value };
      // Auto-compute discount or flash price
      if (field === 'flashPrice') {
        const orig = parseFloat(updated.originalPrice);
        const flash = parseFloat(value);
        if (!isNaN(orig) && !isNaN(flash) && orig > 0 && flash > 0) {
          updated.discountPct = ((orig - flash) / orig * 100).toFixed(1);
        }
      }
      if (field === 'discountPct') {
        const orig = parseFloat(updated.originalPrice);
        const pct = parseFloat(value);
        if (!isNaN(orig) && !isNaN(pct) && pct > 0 && pct < 100) {
          updated.flashPrice = (orig * (1 - pct / 100)).toFixed(0);
        }
      }
      return updated;
    }));
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    let createdId = null;
    try {
      // Step A: Create the flash sale
      const payload = {
        ...scheduleForm,
        startTime: new Date(scheduleForm.startTime).toISOString(),
        endTime: new Date(scheduleForm.endTime).toISOString(),
        isActive: false // scheduler will auto-activate when time comes
      };
      const created = await adminApi.createFlashSale(payload);
      createdId = created?.id;

      if (!createdId) throw new Error('Không nhận được ID Flash Sale sau khi tạo');

      // Step B: Add all products
      let successCount = 0;
      for (const item of selectedItems) {
        try {
          await adminApi.addFlashSaleProduct(createdId, {
            productId: item.productId,
            originalPrice: parseFloat(item.originalPrice),
            flashPrice: parseFloat(item.flashPrice),
            stockLimit: parseInt(item.stockLimit),
            stockSold: 0,
            maxPerCustomer: parseInt(item.maxPerCustomer) || 1,
            displayOrder: item.displayOrder || 0,
            isActive: true
          });
          successCount++;
        } catch (err) {
          toast.error(`Lỗi khi thêm "${item.name}": ${err.message}`);
        }
      }

      toast.success(`Đã tạo Flash Sale với ${successCount}/${selectedItems.length} sản phẩm. Sẽ tự động kích hoạt khi đến giờ.`);
      onDone?.();
      handleClose();
    } catch (err) {
      toast.error(err.message || 'Lỗi khi tạo Flash Sale');
      // If flash sale was created but products failed, let user know it still exists
      if (createdId) {
        toast.error(`Flash Sale đã tạo (id=${createdId}) nhưng có lỗi khi thêm sản phẩm. Vui lòng thêm sản phẩm thủ công.`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    setStep(STEP_SCHEDULE);
    setScheduleForm({ name: '', description: '', startTime: '', endTime: '', backgroundColor: '#EF4444', bannerImageUrl: '', isActive: false });
    setScheduleErrors({});
    setSelectedItems([]);
    setSearchQuery('');
    onClose();
  };

  // Products already filtered server-side; only exclude already-selected ones client-side
  const filteredProducts = allProducts.filter(
    p => !selectedItems.some(it => it.productId === p.id)
  );

  if (!isOpen) return null;

  const stepLabels = ['Lên lịch', 'Chọn sản phẩm', 'Xác nhận'];

  const formatNum = (n) => {
    const num = parseFloat(n);
    if (isNaN(num)) return '—';
    return num.toLocaleString('vi-VN') + '₫';
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-3xl max-h-[92vh] overflow-hidden flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-red-600 to-orange-500">
          <div className="flex items-center gap-3">
            <Zap size={22} className="text-white" />
            <h2 className="text-xl font-bold text-white">Lên lịch Flash Sale</h2>
          </div>
          <button onClick={handleClose} className="text-white/70 hover:text-white transition">
            <X size={22} />
          </button>
        </div>

        {/* Step indicator */}
        <div className="flex items-center px-6 py-3 bg-gray-50 border-b border-gray-100">
          {stepLabels.map((label, idx) => {
            const num = idx + 1;
            const active = step === num;
            const done = step > num;
            return (
              <React.Fragment key={num}>
                <div className="flex items-center gap-2">
                  <div className={`w-7 h-7 rounded-full flex items-center justify-center text-sm font-bold transition
                    ${done ? 'bg-green-500 text-white' : active ? 'bg-red-600 text-white' : 'bg-gray-200 text-gray-400'}`}>
                    {done ? <Check size={14} /> : num}
                  </div>
                  <span className={`text-sm font-medium transition ${active ? 'text-red-600' : done ? 'text-green-600' : 'text-gray-400'}`}>
                    {label}
                  </span>
                </div>
                {idx < stepLabels.length - 1 && (
                  <div className={`flex-1 mx-3 h-0.5 ${step > num ? 'bg-green-400' : 'bg-gray-200'}`} />
                )}
              </React.Fragment>
            );
          })}
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-6">
          {/* Step 1: Schedule */}
          {step === STEP_SCHEDULE && (
            <div className="space-y-4 max-w-xl mx-auto">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tên chương trình <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={scheduleForm.name}
                  onChange={e => setScheduleForm(f => ({ ...f, name: e.target.value }))}
                  className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent ${scheduleErrors.name ? 'border-red-400' : 'border-gray-300'}`}
                  placeholder="VD: Flash Sale Cuối Tuần"
                />
                {scheduleErrors.name && <p className="text-red-500 text-xs mt-1">{scheduleErrors.name}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
                <textarea
                  value={scheduleForm.description}
                  onChange={e => setScheduleForm(f => ({ ...f, description: e.target.value }))}
                  rows={2}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  placeholder="Mô tả ngắn về chương trình..."
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Bắt đầu <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="datetime-local"
                    value={scheduleForm.startTime}
                    onChange={e => setScheduleForm(f => ({ ...f, startTime: e.target.value }))}
                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent ${scheduleErrors.startTime ? 'border-red-400' : 'border-gray-300'}`}
                  />
                  {scheduleErrors.startTime && <p className="text-red-500 text-xs mt-1">{scheduleErrors.startTime}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Kết thúc <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="datetime-local"
                    value={scheduleForm.endTime}
                    onChange={e => setScheduleForm(f => ({ ...f, endTime: e.target.value }))}
                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent ${scheduleErrors.endTime ? 'border-red-400' : 'border-gray-300'}`}
                  />
                  {scheduleErrors.endTime && <p className="text-red-500 text-xs mt-1">{scheduleErrors.endTime}</p>}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Màu nền</label>
                <div className="flex items-center gap-2">
                  <input
                    type="color"
                    value={scheduleForm.backgroundColor}
                    onChange={e => setScheduleForm(f => ({ ...f, backgroundColor: e.target.value }))}
                    className="w-12 h-10 border border-gray-300 rounded cursor-pointer"
                  />
                  <input
                    type="text"
                    value={scheduleForm.backgroundColor}
                    onChange={e => setScheduleForm(f => ({ ...f, backgroundColor: e.target.value }))}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    placeholder="#EF4444"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Banner URL (tùy chọn)</label>
                <input
                  type="text"
                  value={scheduleForm.bannerImageUrl}
                  onChange={e => setScheduleForm(f => ({ ...f, bannerImageUrl: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  placeholder="https://..."
                />
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 flex items-start gap-2">
                <AlertCircle size={16} className="text-blue-500 mt-0.5 flex-shrink-0" />
                <p className="text-sm text-blue-700">
                  Flash Sale sẽ <strong>tự động kích hoạt</strong> khi đến giờ bắt đầu (job chạy mỗi 10 giây). Bạn không cần bật thủ công.
                </p>
              </div>
            </div>
          )}

          {/* Step 2: Products */}
          {step === STEP_PRODUCTS && (
            <div className="space-y-4">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">
                  Đã chọn <strong className="text-red-600">{selectedItems.length}</strong> sản phẩm
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                {/* Left: product list */}
                <div className="border border-gray-200 rounded-xl overflow-hidden flex flex-col max-h-[420px]">
                  <div className="p-3 bg-gray-50 border-b border-gray-200">
                    <div className="relative">
                      <Search size={15} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
                      <input
                        type="text"
                        value={searchQuery}
                        onChange={e => setSearchQuery(e.target.value)}
                        placeholder="Tìm sản phẩm..."
                        className="w-full pl-8 pr-3 py-1.5 text-sm border border-gray-300 rounded-lg focus:ring-1 focus:ring-red-500 focus:border-red-500"
                      />
                    </div>
                  </div>
                  <div className="flex-1 overflow-y-auto">
                    {productsLoading ? (
                      <div className="text-center py-8 text-gray-400">Đang tải...</div>
                    ) : filteredProducts.length === 0 ? (
                      <div className="text-center py-8 text-gray-400 text-sm">Không tìm thấy sản phẩm</div>
                    ) : (
                      filteredProducts.map(p => (
                        <div
                          key={p.id}
                          onClick={() => addProduct(p)}
                          className="flex items-center gap-2 px-3 py-2 hover:bg-red-50 cursor-pointer border-b border-gray-100 last:border-0 transition group"
                        >
                          <img src={p.productImages?.[0]?.imageUrl || '/images/placeholder.png'} alt={p.name} className="w-10 h-10 object-cover rounded" />
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-gray-800 truncate">{p.name}</p>
                            <p className="text-xs text-gray-500">{parseFloat(p.price || 0).toLocaleString('vi-VN')}₫ · Kho: {p.stockQuantity || p.quantity || 0}</p>
                          </div>
                          <Plus size={16} className="text-gray-300 group-hover:text-red-500 transition flex-shrink-0" />
                        </div>
                      ))
                    )}
                  </div>
                  {/* Pagination controls */}
                  {totalProductPages > 1 && (
                    <div className="flex items-center justify-between px-3 py-2 border-t border-gray-200 bg-gray-50">
                      <button
                        onClick={() => setProductPage(p => Math.max(0, p - 1))}
                        disabled={productPage === 0 || productsLoading}
                        className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-white transition"
                      >‹ Trước</button>
                      <span className="text-xs text-gray-500">{productPage + 1} / {totalProductPages}</span>
                      <button
                        onClick={() => setProductPage(p => Math.min(totalProductPages - 1, p + 1))}
                        disabled={productPage === totalProductPages - 1 || productsLoading}
                        className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-white transition"
                      >Sau ›</button>
                    </div>
                  )}
                </div>

                {/* Right: selected products with pricing */}
                <div className="border border-gray-200 rounded-xl overflow-hidden flex flex-col max-h-[420px]">
                  <div className="p-3 bg-red-50 border-b border-gray-200">
                    <p className="text-sm font-semibold text-red-700 flex items-center gap-1.5">
                      <Package size={15} />
                      Sản phẩm đã chọn ({selectedItems.length})
                    </p>
                  </div>
                  <div className="flex-1 overflow-y-auto">
                    {selectedItems.length === 0 ? (
                      <div className="text-center py-8 text-gray-400 text-sm">
                        <Package size={28} className="mx-auto mb-2 opacity-40" />
                        Chọn sản phẩm từ danh sách bên trái
                      </div>
                    ) : (
                      selectedItems.map(item => (
                        <div key={item.productId} className="p-3 border-b border-gray-100 last:border-0">
                          <div className="flex items-center gap-2 mb-2">
                            <img src={item.imageUrl || '/images/placeholder.png'} alt={item.name} className="w-8 h-8 object-cover rounded" />
                            <p className="text-sm font-medium text-gray-800 flex-1 truncate">{item.name}</p>
                            <button onClick={() => removeProduct(item.productId)} className="text-red-400 hover:text-red-600 transition">
                              <Trash2 size={14} />
                            </button>
                          </div>
                          <div className="grid grid-cols-3 gap-1.5">
                            <div>
                              <label className="text-xs text-gray-500">Giá Flash (₫)</label>
                              <input
                                type="number"
                                value={item.flashPrice}
                                onChange={e => updateItem(item.productId, 'flashPrice', e.target.value)}
                                placeholder="0"
                                className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:ring-1 focus:ring-red-500"
                              />
                            </div>
                            <div>
                              <label className="text-xs text-gray-500">Giảm (%)</label>
                              <input
                                type="number"
                                value={item.discountPct}
                                onChange={e => updateItem(item.productId, 'discountPct', e.target.value)}
                                placeholder="0"
                                min="1" max="99"
                                className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:ring-1 focus:ring-red-500"
                              />
                            </div>
                            <div>
                              <label className="text-xs text-gray-500">Kho Flash</label>
                              <input
                                type="number"
                                value={item.stockLimit}
                                onChange={e => updateItem(item.productId, 'stockLimit', e.target.value)}
                                placeholder="0"
                                min="1"
                                className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:ring-1 focus:ring-red-500"
                              />
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Confirm */}
          {step === STEP_CONFIRM && (
            <div className="space-y-5 max-w-xl mx-auto">
              {/* Schedule summary */}
              <div className="bg-red-50 border border-red-200 rounded-xl p-4">
                <div className="flex items-center gap-2 mb-3">
                  <Zap size={18} className="text-red-600" />
                  <h3 className="font-bold text-red-800">{scheduleForm.name}</h3>
                </div>
                {scheduleForm.description && (
                  <p className="text-sm text-red-700 mb-2">{scheduleForm.description}</p>
                )}
                <div className="grid grid-cols-2 gap-2 text-sm">
                  <div className="flex items-center gap-2 text-gray-700">
                    <Calendar size={14} className="text-red-500" />
                    <span>Bắt đầu: {new Date(scheduleForm.startTime).toLocaleString('vi-VN')}</span>
                  </div>
                  <div className="flex items-center gap-2 text-gray-700">
                    <Clock size={14} className="text-red-500" />
                    <span>Kết thúc: {new Date(scheduleForm.endTime).toLocaleString('vi-VN')}</span>
                  </div>
                </div>
              </div>

              {/* Product summary */}
              <div>
                <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                  <Package size={16} />
                  {selectedItems.length} sản phẩm sẽ được thêm
                </h3>
                <div className="space-y-2">
                  {selectedItems.map(item => (
                    <div key={item.productId} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                      <img src={item.imageUrl || '/images/placeholder.png'} alt={item.name} className="w-10 h-10 object-cover rounded" />
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-800">{item.name}</p>
                        <p className="text-xs text-gray-500">
                          <span className="line-through text-gray-400">{formatNum(item.originalPrice)}</span>
                          {' → '}
                          <span className="font-bold text-red-600">{formatNum(item.flashPrice)}</span>
                          {' · '}
                          <span className="text-green-600">-{item.discountPct}%</span>
                          {' · '}
                          <span>Kho: {item.stockLimit}</span>
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="bg-green-50 border border-green-200 rounded-lg p-3 flex items-start gap-2">
                <Check size={16} className="text-green-600 mt-0.5 flex-shrink-0" />
                <p className="text-sm text-green-700">
                  Flash Sale sẽ được lưu với trạng thái <strong>chờ kích hoạt</strong>. 
                  Job tự động sẽ bật Flash Sale lên trang chủ đúng giờ.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Footer actions */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-gray-100 bg-gray-50">
          <button
            onClick={step === STEP_SCHEDULE ? handleClose : handleBack}
            disabled={submitting}
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 transition text-gray-700 disabled:opacity-50"
          >
            <ChevronLeft size={18} />
            {step === STEP_SCHEDULE ? 'Hủy' : 'Quay lại'}
          </button>

          {step < STEP_CONFIRM ? (
            <button
              onClick={handleNext}
              className="flex items-center gap-2 px-5 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium"
            >
              Tiếp theo
              <ChevronRight size={18} />
            </button>
          ) : (
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="flex items-center gap-2 px-5 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium disabled:opacity-50"
            >
              {submitting ? (
                <><span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Đang tạo...</>
              ) : (
                <><Check size={18} />Xác nhận & Lên lịch</>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default FlashSaleWizardModal;
