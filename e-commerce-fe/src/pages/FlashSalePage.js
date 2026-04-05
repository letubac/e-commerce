/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api, { getImageUrl } from '../api/api';
import toast from '../utils/toast';
import { Zap, Clock, ShoppingCart, Flame, Tag, Filter } from 'lucide-react';
import CountdownTimer from '../components/CountdownTimer';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200' viewBox='0 0 300 200'%3E%3Crect width='300' height='200' fill='%23f0f0f0'/%3E%3Ctext x='150' y='106' text-anchor='middle' fill='%23999' font-family='sans-serif' font-size='14'%3ENo Image%3C/text%3E%3C/svg%3E";

function FlashSalePage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [sessions, setSessions] = useState([]);
  const [selectedSession, setSelectedSession] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [productsLoading, setProductsLoading] = useState(false);
  const [timeLeft, setTimeLeft] = useState({ hours: 0, minutes: 0, seconds: 0 });
  const [sortBy, setSortBy] = useState('default');
  const [addingToCart, setAddingToCart] = useState(null);

  const isActive = (session) => {
    const now = new Date();
    return now >= new Date(session.startTime) && now <= new Date(session.endTime);
  };

  const isUpcoming = (session) => new Date() < new Date(session.startTime);


  const calcDiscount = (originalPrice, flashPrice) => {
    if (!originalPrice || !flashPrice || originalPrice <= flashPrice) return 0;
    return Math.round((1 - flashPrice / originalPrice) * 100);
  };

  const formatTimeRange = (start, end) => {
    if (!start || !end) return '';
    const s = new Date(start);
    const e = new Date(end);
    return `${s.getHours().toString().padStart(2,'0')}:${s.getMinutes().toString().padStart(2,'0')} - ${e.getHours().toString().padStart(2,'0')}:${e.getMinutes().toString().padStart(2,'0')}`;
  };

  const getProductImageUrl = (imageUrl) => getImageUrl(imageUrl);

  const fetchSessions = useCallback(async () => {
    try {
      setLoading(true);
      const data = await api.request('/flash-sale/schedule').catch(() => []);
      const list = Array.isArray(data) ? data : [];
      setSessions(list);
      if (list.length > 0) {
        const active = list.find(s => isActive(s));
        const defaultSession = active || list.find(s => isUpcoming(s)) || list[0];
        // Only update selectedSession when the id actually changes to prevent infinite loop
        setSelectedSession(prev => (prev?.id === defaultSession?.id ? prev : defaultSession));
      }
    } catch (error) {
      console.error('Error fetching flash sale schedule:', error);
    } finally {
      setLoading(false);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchProducts = useCallback(async (sessionId) => {
    if (!sessionId) return;
    try {
      setProductsLoading(true);
      const data = await api.request(`/flash-sale/${sessionId}/products`).catch(() => []);
      setProducts(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching products:', error);
      setProducts([]);
    } finally {
      setProductsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSessions();
  }, [fetchSessions]);

  useEffect(() => {
    if (selectedSession?.id) {
      fetchProducts(selectedSession.id);
    }
  }, [selectedSession, fetchProducts]);

  // Countdown timer
  useEffect(() => {
    if (!selectedSession) return;
    let refetchFired = false; // guard: only refetch once per expiry event
    const updateTimer = () => {
      const now = Date.now();
      const targetTime = isActive(selectedSession)
        ? new Date(selectedSession.endTime).getTime()
        : new Date(selectedSession.startTime).getTime();
      const distance = targetTime - now;
      if (distance <= 0) {
        setTimeLeft({ hours: 0, minutes: 0, seconds: 0 });
        if (!refetchFired) {
          refetchFired = true;
          fetchSessions();
        }
        return;
      }
      setTimeLeft({
        hours: Math.floor(distance / (1000 * 60 * 60)),
        minutes: Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60)),
        seconds: Math.floor((distance % (1000 * 60)) / 1000),
      });
    };
    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    return () => clearInterval(interval);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedSession]);

  const handleAddToCart = async (product, e) => {
    e.stopPropagation();
    if (!user) {
      navigate('/login');
      return;
    }
    setAddingToCart(product.productId);
    try {
      await api.addToCart({ productId: product.productId, quantity: 1 });
      toast.success('Đã thêm vào giỏ hàng!');
    } catch (error) {
      toast.error(error.message || 'Không thể thêm vào giỏ hàng');
    } finally {
      setAddingToCart(null);
    }
  };

  const getSortedProducts = () => {
    const list = [...products];
    switch (sortBy) {
      case 'discount':
        return list.sort((a, b) =>
          calcDiscount(b.originalPrice, b.flashPrice) - calcDiscount(a.originalPrice, a.flashPrice)
        );
      case 'price':
        return list.sort((a, b) => (a.flashPrice || 0) - (b.flashPrice || 0));
      case 'sold':
        return list.sort((a, b) => (b.stockSold || 0) - (a.stockSold || 0));
      default:
        return list;
    }
  };

  const sortedProducts = getSortedProducts();
  const selectedIsActive = selectedSession ? isActive(selectedSession) : false;

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100">
        <div className="bg-gradient-to-r from-red-600 to-red-700 h-40 animate-pulse" />
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {[...Array(10)].map((_, i) => (
              <div key={i} className="bg-white rounded-lg animate-pulse h-72" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Hero header */}
      <div className="bg-gradient-to-r from-red-600 to-red-700 text-white py-6 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <Flame className="w-10 h-10 text-yellow-300 fill-yellow-300" />
              <div>
                <h1 className="text-3xl font-bold uppercase tracking-wide">Flash Sale</h1>
                <p className="text-white/80 text-sm mt-0.5">
                  {selectedSession?.name || 'Ưu đãi giới hạn - Nhanh tay kẻo lỡ!'}
                </p>
              </div>
            </div>

            {/* Countdown */}
            {selectedSession && (
              <div className="flex flex-col items-center gap-1">
                <span className="text-white/80 text-xs flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5" />
                  {selectedIsActive ? 'Kết thúc sau' : 'Bắt đầu sau'}
                </span>
                <CountdownTimer
                  hours={timeLeft.hours}
                  minutes={timeLeft.minutes}
                  seconds={timeLeft.seconds}
                  boxClassName="bg-white text-red-600 font-bold text-2xl px-3 py-1 rounded-lg min-w-[52px] text-center"
                  labelClassName="hidden"
                />
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Session tabs */}
      {sessions.length > 0 && (
        <div className="bg-white border-b border-gray-200 shadow-sm">
          <div className="max-w-7xl mx-auto px-4">
            <div className="flex gap-1 overflow-x-auto py-2 scrollbar-hide">
              {sessions.map((session) => {
                const active = isActive(session);
                const upcoming = isUpcoming(session);
                const selected = selectedSession?.id === session.id;
                return (
                  <button
                    key={session.id}
                    onClick={() => setSelectedSession(session)}
                    className={`flex-shrink-0 px-4 py-2 rounded-lg text-sm font-medium transition flex flex-col items-center min-w-[90px] border ${
                      selected
                        ? 'bg-red-600 text-white border-red-600'
                        : 'bg-white text-gray-700 border-gray-200 hover:border-red-300'
                    }`}
                  >
                    <span>{formatTimeRange(session.startTime, session.endTime)}</span>
                    <span className={`text-xs mt-0.5 ${selected ? 'text-white/80' : active ? 'text-green-600' : 'text-gray-400'}`}>
                      {active ? 'Đang diễn ra' : upcoming ? 'Sắp diễn ra' : 'Đã kết thúc'}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      )}

      <div className="max-w-7xl mx-auto px-4 py-6">
        {/* Filter bar */}
        {products.length > 0 && (
          <div className="bg-white rounded-lg px-4 py-3 mb-4 flex items-center gap-3 flex-wrap shadow-sm">
            <div className="flex items-center gap-1 text-gray-600 text-sm font-medium">
              <Filter className="w-4 h-4" />
              <span>Sắp xếp:</span>
            </div>
            {[
              { key: 'default', label: 'Tất cả' },
              { key: 'discount', label: 'Giảm nhiều nhất' },
              { key: 'price', label: 'Giá tăng dần' },
              { key: 'sold', label: 'Bán chạy nhất' },
            ].map(opt => (
              <button
                key={opt.key}
                onClick={() => setSortBy(opt.key)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition border ${
                  sortBy === opt.key
                    ? 'bg-red-600 text-white border-red-600'
                    : 'bg-white text-gray-600 border-gray-200 hover:border-red-300'
                }`}
              >
                {opt.label}
              </button>
            ))}
            <span className="ml-auto text-sm text-gray-400">{products.length} sản phẩm</span>
          </div>
        )}

        {/* Product grid */}
        {productsLoading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {[...Array(10)].map((_, i) => (
              <div key={i} className="bg-white rounded-lg animate-pulse h-72" />
            ))}
          </div>
        ) : sortedProducts.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {sortedProducts.map((product) => {
              const discount = product.discountPercentage
                ? Math.round(product.discountPercentage)
                : calcDiscount(product.originalPrice, product.flashPrice);
              const stockLeft = (product.stockLimit || 0) - (product.stockSold || 0);
              const soldPct = product.stockLimit
                ? Math.min(100, Math.round(((product.stockSold || 0) / product.stockLimit) * 100))
                : 0;
              const isSoldOut = stockLeft <= 0;
              const saved = product.originalPrice && product.flashPrice
                ? product.originalPrice - product.flashPrice
                : 0;
              const imgUrl = getProductImageUrl(product.productImageUrl);
              const isAdding = addingToCart === product.productId;

              return (
                <div
                  key={product.id || product.productId}
                  className="bg-white rounded-lg overflow-hidden shadow-sm hover:shadow-md transition cursor-pointer flex flex-col"
                  onClick={() => navigate(`/product/${product.productId}`)}
                >
                  {/* Image */}
                  <div className="relative">
                    <img
                      src={imgUrl}
                      alt={product.productName || product.name}
                      className="w-full object-cover"
                      style={{ height: '200px' }}
                      onError={e => { e.target.src = PLACEHOLDER_IMG; }}
                    />
                    {discount > 0 && (
                      <div className="absolute top-2 left-2 bg-red-600 text-white text-xs font-bold px-2 py-0.5 rounded">
                        -{discount}%
                      </div>
                    )}
                    {isSoldOut && (
                      <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                        <span className="bg-white text-gray-700 font-bold text-sm px-3 py-1 rounded-full">Hết hàng</span>
                      </div>
                    )}
                  </div>

                  {/* Info */}
                  <div className="p-3 flex flex-col flex-1">
                    <h3 className="text-sm text-gray-800 font-medium line-clamp-2 mb-2 flex-1">
                      {product.productName || product.name}
                    </h3>

                    <div className="mb-2">
                      <p className="text-red-600 font-bold text-lg leading-tight">
                        {(product.flashPrice || 0).toLocaleString('vi-VN')}đ
                      </p>
                      <p className="text-gray-400 text-xs line-through">
                        {(product.originalPrice || 0).toLocaleString('vi-VN')}đ
                      </p>
                      {saved > 0 && (
                        <div className="inline-flex items-center gap-1 bg-orange-50 text-orange-600 text-xs px-1.5 py-0.5 rounded mt-1">
                          <Tag className="w-3 h-3" />
                          Tiết kiệm {saved.toLocaleString('vi-VN')}đ
                        </div>
                      )}
                    </div>

                    {/* Progress bar */}
                    <div className="mb-2">
                      <div className="flex justify-between text-xs text-gray-500 mb-1">
                        <span>Đã bán {product.stockSold || 0}</span>
                        {stockLeft > 0 && <span>Còn {stockLeft}</span>}
                      </div>
                      <div className="bg-gray-100 rounded-full h-1.5">
                        <div
                          className={`h-1.5 rounded-full transition-all ${soldPct >= 80 ? 'bg-orange-500' : 'bg-red-500'}`}
                          style={{ width: `${soldPct}%` }}
                        />
                      </div>
                    </div>

                    {product.maxPurchasePerCustomer && (
                      <p className="text-xs text-gray-400 mb-2">
                        Tối đa {product.maxPurchasePerCustomer} sản phẩm/khách
                      </p>
                    )}

                    <button
                      disabled={isSoldOut || isAdding}
                      onClick={e => handleAddToCart(product, e)}
                      className={`w-full py-2 rounded-lg text-sm font-semibold transition flex items-center justify-center gap-1.5 ${
                        isSoldOut
                          ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                          : isAdding
                            ? 'bg-red-400 text-white cursor-wait'
                            : 'bg-red-600 text-white hover:bg-red-700'
                      }`}
                    >
                      <ShoppingCart className="w-4 h-4" />
                      {isSoldOut ? 'Hết hàng' : isAdding ? 'Đang thêm...' : 'Thêm vào giỏ'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          /* Empty state */
          <div className="bg-white rounded-lg py-20 text-center shadow-sm">
            <Zap className="w-16 h-16 text-red-200 mx-auto mb-4" />
            {selectedSession ? (
              selectedIsActive ? (
                <>
                  <h3 className="text-gray-700 font-semibold text-lg mb-1">Sắp diễn ra...</h3>
                  <p className="text-gray-500 text-sm">Sản phẩm Flash Sale sẽ sớm được cập nhật</p>
                </>
              ) : (
                <>
                  <h3 className="text-gray-700 font-semibold text-lg mb-1">Sắp diễn ra...</h3>
                  <p className="text-gray-500 text-sm">
                    Phiên bắt đầu lúc{' '}
                    {new Date(selectedSession.startTime).toLocaleString('vi-VN', {
                      hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit',
                    })}
                  </p>
                </>
              )
            ) : (
              <>
                <h3 className="text-gray-500 font-medium text-lg mb-1">Không có Flash Sale</h3>
                <p className="text-gray-400 text-sm">Hiện tại không có chương trình Flash Sale nào</p>
              </>
            )}
            <button
              onClick={() => navigate('/products')}
              className="mt-6 bg-red-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-red-700 transition"
            >
              Xem sản phẩm khác
            </button>
          </div>
        )}

        {/* Upcoming sessions schedule */}
        {sessions.filter(s => isUpcoming(s)).length > 0 && (
          <div className="mt-8 bg-white rounded-lg shadow-sm p-5">
            <h2 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
              <Clock className="w-5 h-5 text-red-600" />
              Lịch Flash Sale sắp tới
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              {sessions.filter(s => isUpcoming(s)).map(session => (
                <div
                  key={session.id}
                  onClick={() => setSelectedSession(session)}
                  className="border border-gray-200 rounded-lg p-3 cursor-pointer hover:border-red-300 hover:bg-red-50 transition"
                >
                  <div className="flex items-center gap-2">
                    <div className="bg-red-100 text-red-600 p-1.5 rounded-lg">
                      <Zap className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-gray-800">
                        {formatTimeRange(session.startTime, session.endTime)}
                      </p>
                      <p className="text-xs text-gray-400">
                        {new Date(session.startTime).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })}
                      </p>
                    </div>
                  </div>
                  {session.name && <p className="text-xs text-gray-500 mt-1.5 truncate">{session.name}</p>}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default FlashSalePage;
