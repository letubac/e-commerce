import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, CreditCard, Truck, Shield, CheckCircle } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import api, { API_BASE_URL } from '../api/api';
import toast from '../utils/toast';

function CheckoutPage() {
  const navigate = useNavigate();
  const { cartItems, getTotalPrice, clearCart } = useCart();
  const { user } = useAuth();
  
  const [step, setStep] = useState(1); // 1: Info, 2: Payment, 3: Confirmation
  const [loading, setLoading] = useState(false);
  const [orderSuccess, setOrderSuccess] = useState(false);
  const [orderId, setOrderId] = useState(null);
  const [orderNumber, setOrderNumber] = useState(null);
  const [savedOrderTotal, setSavedOrderTotal] = useState(0); // Save total before clearing cart

  // Form states
  const [customerInfo, setCustomerInfo] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: '',
    city: '',
    district: '',
    ward: '',
    notes: ''
  });

  const [paymentMethod, setPaymentMethod] = useState('cod'); // cod, vnpay, momo
  const [shippingMethod, setShippingMethod] = useState('standard');

  const safeCartItems = cartItems || [];
  const totalPrice = getTotalPrice() || 0;
  const shippingFee = shippingMethod === 'express' ? 50000 : 0;
  const finalTotal = totalPrice + shippingFee;

  useEffect(() => {
    // Don't redirect if order was just placed successfully
    if (safeCartItems.length === 0 && !orderSuccess) {
      navigate('/cart');
    }
  }, [safeCartItems, navigate, orderSuccess]);

  const handleInputChange = (field, value) => {
    setCustomerInfo(prev => ({ ...prev, [field]: value }));
  };

  const handleNextStep = () => {
    if (step === 1) {
      // Validate customer info
      if (!customerInfo.fullName || !customerInfo.phone || !customerInfo.address) {
        toast.error('Vui lòng điền đầy đủ thông tin bắt buộc (Họ tên, Số điện thoại, Địa chỉ)');
        return;
      }
      
      // Validate phone number format
      const phoneRegex = /^[0-9]{10,11}$/;
      if (!phoneRegex.test(customerInfo.phone.replace(/\s+/g, ''))) {
        toast.error('Số điện thoại không hợp lệ. Vui lòng nhập 10-11 số.');
        return;
      }
      
      // Validate email if provided
      if (customerInfo.email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(customerInfo.email)) {
          toast.error('Email không hợp lệ. Vui lòng kiểm tra lại.');
          return;
        }
      }
      
      setStep(2);
    } else if (step === 2) {
      // Validate cart items before placing order
      if (safeCartItems.length === 0) {
        toast.error('Giỏ hàng trống. Không thể đặt hàng.');
        navigate('/cart');
        return;
      }
      
      handlePlaceOrder();
    }
  };

  const handlePlaceOrder = async () => {
    setLoading(true);
    try {
      // Prepare order data
      const orderData = {
        customerInfo: {
          fullName: customerInfo.fullName,
          email: customerInfo.email,
          phone: customerInfo.phone,
          address: customerInfo.address,
          city: customerInfo.city,
          district: customerInfo.district,
          ward: customerInfo.ward,
          notes: customerInfo.notes
        },
        shippingMethod: shippingMethod,
        paymentMethod: paymentMethod,
        items: safeCartItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price
        })),
        totalPrice: finalTotal,
        shippingFee: shippingFee
      };

      console.log('Placing order with data:', orderData);
      
      // Call API to create order
      const response = await api.createOrder(orderData);
      console.log('Order created successfully:', response);
      
      setOrderId(response.id || response.orderId);
      setOrderNumber(response.orderNumber || response.orderCode);
      setSavedOrderTotal(finalTotal); // Save total before clearing cart
      setOrderSuccess(true);
      setStep(3);
      
      // Track order creation analytics (optional)
      if (window.gtag) {
        window.gtag('event', 'purchase', {
          transaction_id: response.id || response.orderId,
          value: finalTotal,
          currency: 'VND',
          items: safeCartItems.map(item => ({
            item_id: item.productId,
            item_name: item.productName,
            quantity: item.quantity,
            price: item.price
          }))
        });
      }
      
      // Clear cart after successful order (silent mode - no toast)
      setTimeout(() => {
        clearCart(true);
      }, 1000);
      
    } catch (error) {
      console.error('Error placing order:', error);
      
      // Check if it's a 401 authentication error (already handled by api.js)
      if (error.message === 'Authentication required') {
        // api.js already showed logout message and redirecting
        return;
      }
      
      // For all other errors, show checkout-specific error message
      const errorMsg = error.message || 'Đã xảy ra lỗi không xác định';
      toast.error(`Lỗi xảy ra trong quá trình đặt hàng: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  if (orderSuccess && step === 3) {
    return (
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-lg p-8 text-center">
            <div className="mb-6">
              <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
              <h1 className="text-3xl font-bold text-gray-800 mb-2">Đặt hàng thành công!</h1>
              <p className="text-gray-600">Cảm ơn bạn đã mua sắm tại E-SHOP</p>
            </div>
            
            <div className="bg-gray-50 rounded-lg p-6 mb-6">
              <h3 className="font-semibold text-lg mb-4">Thông tin đơn hàng</h3>
              <div className="text-left space-y-2">
                <div className="flex justify-between">
                  <span>Mã đơn hàng:</span>
                  <span className="font-semibold">#{orderNumber || orderId || `ES${Date.now().toString().slice(-6)}`}</span>
                </div>
                <div className="flex justify-between">
                  <span>Tổng tiền:</span>
                  <span className="font-semibold text-red-600">{savedOrderTotal.toLocaleString('vi-VN')}₫</span>
                </div>
                <div className="flex justify-between">
                  <span>Phương thức thanh toán:</span>
                  <span className="font-semibold">
                    {paymentMethod === 'cod' ? 'Thanh toán khi nhận hàng' : 
                     paymentMethod === 'vnpay' ? 'VNPay' : 'MoMo'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span>Phương thức vận chuyển:</span>
                  <span className="font-semibold">
                    {shippingMethod === 'standard' ? 'Giao hàng tiêu chuẩn (3-5 ngày)' : 'Giao hàng nhanh (1-2 ngày)'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span>Địa chỉ giao hàng:</span>
                  <span className="font-semibold text-sm">
                    {customerInfo.address}, {customerInfo.district}, {customerInfo.city}
                  </span>
                </div>
              </div>
            </div>

            <div className="bg-blue-50 rounded-lg p-4 mb-6">
              <div className="flex items-start">
                <CheckCircle className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
                <div>
                  <div className="font-medium text-blue-800">Đơn hàng đã được ghi nhận</div>
                  <div className="text-sm text-blue-600 mt-1">
                    Chúng tôi sẽ liên hệ với bạn trong vòng 24h để xác nhận đơn hàng. 
                    Bạn có thể theo dõi tình trạng đơn hàng trong mục "Đơn hàng của tôi".
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <button
                onClick={() => navigate('/orders')}
                className="w-full py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-semibold"
              >
                Xem đơn hàng của tôi
              </button>
              <button
                onClick={() => navigate('/')}
                className="w-full py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
              >
                Tiếp tục mua sắm
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex items-center mb-6">
          <button
            onClick={() => navigate('/cart')}
            className="mr-4 p-2 hover:bg-gray-200 rounded-lg transition"
          >
            <ArrowLeft size={24} />
          </button>
          <h1 className="text-2xl font-bold text-gray-800">Thanh toán</h1>
        </div>

        {/* Progress Steps */}
        <div className="max-w-4xl mx-auto mb-8">
          <div className="flex items-center justify-center space-x-8">
            <div className={`flex items-center space-x-2 ${step >= 1 ? 'text-red-600' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 1 ? 'bg-red-600 text-white' : 'bg-gray-300'}`}>1</div>
              <span className="font-medium">Thông tin</span>
            </div>
            <div className={`w-16 h-0.5 ${step >= 2 ? 'bg-red-600' : 'bg-gray-300'}`}></div>
            <div className={`flex items-center space-x-2 ${step >= 2 ? 'text-red-600' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 2 ? 'bg-red-600 text-white' : 'bg-gray-300'}`}>2</div>
              <span className="font-medium">Thanh toán</span>
            </div>
            <div className={`w-16 h-0.5 ${step >= 3 ? 'bg-red-600' : 'bg-gray-300'}`}></div>
            <div className={`flex items-center space-x-2 ${step >= 3 ? 'text-red-600' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 3 ? 'bg-red-600 text-white' : 'bg-gray-300'}`}>3</div>
              <span className="font-medium">Hoàn tất</span>
            </div>
          </div>
        </div>

        <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Step 1: Customer Information */}
            {step === 1 && (
              <div className="bg-white rounded-lg shadow-sm p-6">
                <div className="flex items-center mb-6">
                  <MapPin className="w-6 h-6 text-red-600 mr-2" />
                  <h2 className="text-xl font-semibold">Thông tin giao hàng</h2>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Họ và tên <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={customerInfo.fullName}
                      onChange={(e) => handleInputChange('fullName', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Nhập họ và tên"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Số điện thoại <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="tel"
                      value={customerInfo.phone}
                      onChange={(e) => handleInputChange('phone', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Nhập số điện thoại"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Email
                    </label>
                    <input
                      type="email"
                      value={customerInfo.email}
                      onChange={(e) => handleInputChange('email', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Nhập email"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Địa chỉ <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={customerInfo.address}
                      onChange={(e) => handleInputChange('address', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Số nhà, tên đường"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Thành phố</label>
                    <select
                      value={customerInfo.city}
                      onChange={(e) => handleInputChange('city', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    >
                      <option value="">Chọn thành phố</option>
                      <option value="hanoi">Hà Nội</option>
                      <option value="hcm">TP. Hồ Chí Minh</option>
                      <option value="danang">Đà Nẵng</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Quận/Huyện</label>
                    <input
                      type="text"
                      value={customerInfo.district}
                      onChange={(e) => handleInputChange('district', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Quận/Huyện"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">Ghi chú</label>
                    <textarea
                      value={customerInfo.notes}
                      onChange={(e) => handleInputChange('notes', e.target.value)}
                      rows="3"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Ghi chú cho đơn hàng (tùy chọn)"
                    ></textarea>
                  </div>
                </div>

                {/* Shipping Methods */}
                <div className="mt-8">
                  <h3 className="text-lg font-semibold mb-4 flex items-center">
                    <Truck className="w-5 h-5 mr-2 text-red-600" />
                    Phương thức vận chuyển
                  </h3>
                  <div className="space-y-3">
                    <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                      <input
                        type="radio"
                        value="standard"
                        checked={shippingMethod === 'standard'}
                        onChange={(e) => setShippingMethod(e.target.value)}
                        className="mr-3"
                      />
                      <div className="flex-1">
                        <div className="font-medium">Giao hàng tiêu chuẩn</div>
                        <div className="text-sm text-gray-600">3-5 ngày làm việc</div>
                      </div>
                      <div className="text-green-600 font-semibold">Miễn phí</div>
                    </label>
                    <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                      <input
                        type="radio"
                        value="express"
                        checked={shippingMethod === 'express'}
                        onChange={(e) => setShippingMethod(e.target.value)}
                        className="mr-3"
                      />
                      <div className="flex-1">
                        <div className="font-medium">Giao hàng nhanh</div>
                        <div className="text-sm text-gray-600">1-2 ngày làm việc</div>
                      </div>
                      <div className="text-red-600 font-semibold">50.000₫</div>
                    </label>
                  </div>
                </div>
              </div>
            )}

            {/* Step 2: Payment Method */}
            {step === 2 && (
              <div className="bg-white rounded-lg shadow-sm p-6">
                <div className="flex items-center mb-6">
                  <CreditCard className="w-6 h-6 text-red-600 mr-2" />
                  <h2 className="text-xl font-semibold">Phương thức thanh toán</h2>
                </div>

                <div className="space-y-4">
                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      value="cod"
                      checked={paymentMethod === 'cod'}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      className="mr-3"
                    />
                    <div className="flex-1">
                      <div className="font-medium">Thanh toán khi nhận hàng (COD)</div>
                      <div className="text-sm text-gray-600">Thanh toán bằng tiền mặt khi nhận hàng</div>
                    </div>
                  </label>
                  
                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      value="vnpay"
                      checked={paymentMethod === 'vnpay'}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      className="mr-3"
                    />
                    <div className="flex-1">
                      <div className="font-medium">VNPay</div>
                      <div className="text-sm text-gray-600">Thanh toán qua ví điện tử VNPay</div>
                    </div>
                  </label>

                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      value="momo"
                      checked={paymentMethod === 'momo'}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      className="mr-3"
                    />
                    <div className="flex-1">
                      <div className="font-medium">MoMo</div>
                      <div className="text-sm text-gray-600">Thanh toán qua ví điện tử MoMo</div>
                    </div>
                  </label>
                </div>

                {/* Security Notice */}
                <div className="mt-6 p-4 bg-blue-50 rounded-lg">
                  <div className="flex items-start">
                    <Shield className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
                    <div>
                      <div className="font-medium text-blue-800">Bảo mật thanh toán</div>
                      <div className="text-sm text-blue-600 mt-1">
                        Thông tin thanh toán của bạn được mã hóa và bảo mật tuyệt đối
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Order Summary Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 sticky top-8">
              <h3 className="text-lg font-semibold mb-4">Đơn hàng của bạn</h3>
              
              {/* Products */}
              <div className="space-y-4 mb-6">
                {safeCartItems.map((item) => (
                  <div key={item.id} className="flex items-center space-x-3">
                    <div className="w-16 h-16 flex-shrink-0">
                      <img
                        src={
                          item.productImage
                            ? `${API_BASE_URL}/files${item.productImage}`
                            : "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='64' height='64' viewBox='0 0 64 64'%3E%3Crect width='64' height='64' fill='%23f0f0f0'/%3E%3C/svg%3E"
                        }
                        alt={item.productName || 'Product'}
                        className="w-full h-full object-cover rounded-lg border border-gray-200"
                        onError={(e) => { e.target.onerror = null; e.target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='64' height='64' viewBox='0 0 64 64'%3E%3Crect width='64' height='64' fill='%23f0f0f0'/%3E%3C/svg%3E"; }}
                      />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h4 className="text-sm font-medium text-gray-800 truncate">
                        {item.productName || 'Sản phẩm'}
                      </h4>
                      <p className="text-sm text-gray-600">Số lượng: {item.quantity}</p>
                    </div>
                    <div className="text-sm font-semibold text-gray-800">
                      {((item.price || 0) * item.quantity).toLocaleString('vi-VN')}₫
                    </div>
                  </div>
                ))}
              </div>

              {/* Price Summary */}
              <div className="border-t border-gray-200 pt-4 space-y-3">
                <div className="flex justify-between text-sm">
                  <span>Tạm tính:</span>
                  <span>{totalPrice.toLocaleString('vi-VN')}₫</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Phí vận chuyển:</span>
                  <span className={shippingFee === 0 ? 'text-green-600' : ''}>
                    {shippingFee === 0 ? 'Miễn phí' : `${shippingFee.toLocaleString('vi-VN')}₫`}
                  </span>
                </div>
                <div className="border-t border-gray-200 pt-3">
                  <div className="flex justify-between font-bold text-lg">
                    <span>Tổng cộng:</span>
                    <span className="text-red-600">{finalTotal.toLocaleString('vi-VN')}₫</span>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-6 space-y-3">
                {step === 1 && (
                  <button
                    onClick={handleNextStep}
                    className="w-full py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-semibold"
                  >
                    Tiếp tục
                  </button>
                )}
                
                {step === 2 && (
                  <>
                    <button
                      onClick={handleNextStep}
                      disabled={loading}
                      className="w-full py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                    >
                      {loading ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          Đang xử lý...
                        </>
                      ) : (
                        'Đặt hàng'
                      )}
                    </button>
                    <button
                      onClick={() => setStep(1)}
                      className="w-full py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                    >
                      Quay lại
                    </button>
                  </>
                )}
              </div>

              {/* Security Features */}
              <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                <h4 className="font-semibold text-sm mb-2">Cam kết E-SHOP</h4>
                <ul className="text-xs text-gray-600 space-y-1">
                  <li>• Sản phẩm chính hãng 100%</li>
                  <li>• Bảo hành chính hãng</li>
                  <li>• Đổi trả trong 15 ngày</li>
                  <li>• Hỗ trợ 24/7</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CheckoutPage;