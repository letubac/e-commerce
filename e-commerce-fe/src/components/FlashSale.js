/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api, { getImageUrl } from '../api/api';
import toast from '../utils/toast';
import CountdownTimer from './CountdownTimer';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200' viewBox='0 0 300 200'%3E%3Crect width='300' height='200' fill='%23f0f0f0'/%3E%3Ctext x='150' y='106' text-anchor='middle' fill='%23999' font-family='sans-serif' font-size='14'%3ENo Image%3C/text%3E%3C/svg%3E";

export default function FlashSale() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [activeFlashSale, setActiveFlashSale] = useState(null);
  const [flashSaleProducts, setFlashSaleProducts] = useState([]);
  const [upcomingSales, setUpcomingSales] = useState([]);
  const [selectedSession, setSelectedSession] = useState(null);
  const [timeLeft, setTimeLeft] = useState({ hours: 0, minutes: 0, seconds: 0 });
  const [loading, setLoading] = useState(true);
  const [scrollIndex, setScrollIndex] = useState(0);

  const ITEMS_VISIBLE = 5;

  const getProductImageUrl = (imageUrl) => {
    return getImageUrl(imageUrl) || PLACEHOLDER_IMG;
  };

  const calcDiscount = (originalPrice, flashPrice) => {
    if (!originalPrice || !flashPrice || originalPrice <= flashPrice) return 0;
    return Math.round((1 - flashPrice / originalPrice) * 100);
  };

  const calcSoldPercent = (stockSold, stockQuantity) => {
    if (!stockQuantity) return 0;
    return Math.min(100, Math.round(((stockSold || 0) / stockQuantity) * 100));
  };


  const fetchFlashSaleData = useCallback(async () => {
    try {
      setLoading(true);
      const [activeData, upcomingData] = await Promise.all([
        api.request('/flash-sale/active').catch(() => null),
        api.request('/flash-sale/upcoming').catch(() => []),
      ]);

      const upcoming = Array.isArray(upcomingData) ? upcomingData : [];
      setUpcomingSales(upcoming);

      if (activeData) {
        setActiveFlashSale(activeData);
        setSelectedSession(activeData);
        const productsData = await api.request('/flash-sale/products').catch(() => []);
        setFlashSaleProducts(Array.isArray(productsData) ? productsData : []);
      } else {
        setActiveFlashSale(null);
        setFlashSaleProducts([]);
        if (upcoming.length > 0) {
          setSelectedSession(upcoming[0]);
        }
      }
    } catch (error) {
      console.error('Error fetching flash sale:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchFlashSaleData();
  }, [fetchFlashSaleData]);

  // Countdown timer
  useEffect(() => {
    if (!selectedSession) return;
    let refetchFired = false; // guard: only refetch once per expiry event
    const updateTimer = () => {
      const now = Date.now();
      const targetTime = activeFlashSale
        ? new Date(activeFlashSale.endTime).getTime()
        : new Date(selectedSession.startTime).getTime();
      const distance = targetTime - now;

      if (distance <= 0) {
        setTimeLeft({ hours: 0, minutes: 0, seconds: 0 });
        if (!refetchFired) {
          refetchFired = true;
          fetchFlashSaleData();
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
  }, [selectedSession, activeFlashSale, fetchFlashSaleData]);

  const handleAddToCart = async (product, e) => {
    e.stopPropagation();
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      await api.addToCart({ productId: product.productId, quantity: 1 });
      toast.success('Đã thêm vào giỏ hàng!');
    } catch (error) {
      toast.error(error.message || 'Không thể thêm vào giỏ hàng');
    }
  };

  const visibleProducts = flashSaleProducts.slice(scrollIndex, scrollIndex + ITEMS_VISIBLE);
  const canScrollLeft = scrollIndex > 0;
  const canScrollRight = scrollIndex + ITEMS_VISIBLE < flashSaleProducts.length;

  if (loading) {
    return (
      <div className="bg-white rounded-lg overflow-hidden shadow-sm mb-4">
        <div className="bg-gradient-to-r from-red-600 to-red-500 h-14 animate-pulse" />
        <div className="p-3 flex gap-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex-shrink-0 bg-gray-100 rounded-lg animate-pulse h-64" style={{ width: '175px' }} />
          ))}
        </div>
      </div>
    );
  }

  if (!activeFlashSale && upcomingSales.length === 0) {
    return (
      <div className="bg-white rounded-lg overflow-hidden shadow-sm mb-4">
        <div className="bg-gradient-to-r from-red-600 to-red-500 px-4 py-3 flex items-center gap-2">
          <Zap className="w-6 h-6 text-yellow-300 fill-yellow-300" />
          <span className="text-white font-bold text-xl uppercase tracking-wide">Flash Sale</span>
        </div>
        <div className="py-10 text-center">
          <Zap className="w-10 h-10 text-red-200 mx-auto mb-3" />
          <p className="text-gray-500 font-semibold text-base">Sắp diễn ra...</p>
          <p className="text-sm text-gray-400 mt-1">Chương trình Flash Sale sẽ sớm được cập nhật</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg overflow-hidden shadow-sm mb-4">
      {/* Red gradient header */}
      <div className="bg-gradient-to-r from-red-600 to-red-500 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          {/* Title */}
          <div className="flex items-center gap-1.5">
            <Zap className="w-6 h-6 text-yellow-300 fill-yellow-300" />
            <span className="text-white font-bold text-xl uppercase tracking-wide">Flash Sale</span>
          </div>

          {/* Countdown */}
          <div className="text-white">
            <CountdownTimer
              hours={timeLeft.hours}
              minutes={timeLeft.minutes}
              seconds={timeLeft.seconds}
              label={activeFlashSale ? 'Kết thúc sau' : 'Bắt đầu sau'}
              labelClassName="text-white text-sm opacity-90"
              boxClassName="bg-gray-900 text-white text-base font-bold px-2 py-0.5 rounded min-w-[32px] text-center leading-6"
            />
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Session tabs */}
          {upcomingSales.length > 0 && (
            <div className="flex gap-1.5">
              {upcomingSales.slice(0, 3).map((session) => {
                const t = new Date(session.startTime);
                const label = `${t.getHours().toString().padStart(2,'0')}:${t.getMinutes().toString().padStart(2,'0')}`;
                const isSelected = selectedSession?.id === session.id;
                return (
                  <button
                    key={session.id}
                    onClick={() => setSelectedSession(session)}
                    className={`text-xs px-2 py-1 rounded font-medium transition ${isSelected ? 'bg-white text-red-600 font-bold' : 'bg-red-700 bg-opacity-60 text-white hover:bg-red-800'}`}
                  >
                    {label}
                  </button>
                );
              })}
            </div>
          )}

          <button
            onClick={() => navigate('/flash-sale')}
            className="text-white text-sm flex items-center gap-0.5 hover:underline"
          >
            Xem tất cả
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Products */}
      {activeFlashSale && flashSaleProducts.length > 0 ? (
        <div className="relative px-6 py-3">
          {/* Left arrow */}
          {canScrollLeft && (
            <button
              onClick={() => setScrollIndex(i => i - 1)}
              className="absolute left-0 top-1/2 -translate-y-1/2 z-10 bg-white shadow-lg rounded-full w-9 h-9 flex items-center justify-center hover:bg-gray-50 transition border border-gray-200"
            >
              <ChevronLeft className="w-5 h-5 text-gray-700" />
            </button>
          )}

          <div className="flex gap-2">
            {visibleProducts.map((product) => {
              const discount = product.discountPercentage
                ? Math.round(product.discountPercentage)
                : calcDiscount(product.originalPrice, product.flashPrice);
              const soldPct = calcSoldPercent(product.stockSold, product.stockLimit);
              const isSoldOut = (product.stockLimit || 0) - (product.stockSold || 0) <= 0;
              const imgUrl = getProductImageUrl(product.productImageUrl);

              return (
                <div
                  key={product.id || product.productId}
                  className="flex-1 min-w-0 bg-white border border-gray-100 rounded-lg overflow-hidden hover:shadow-md transition cursor-pointer"
                  style={{ minWidth: '160px', maxWidth: '200px' }}
                  onClick={() => navigate(`/product/${product.productId}`)}
                >
                  {/* Image + discount badge + progress bar */}
                  <div className="relative">
                    <img
                      src={imgUrl}
                      alt={product.productName || product.name}
                      className="w-full object-cover"
                      style={{ height: '180px' }}
                      onError={e => { e.target.src = PLACEHOLDER_IMG; }}
                    />
                    {discount > 0 && (
                      <div className="absolute top-1.5 left-1.5 bg-red-600 text-white text-xs font-bold px-1.5 py-0.5 rounded">
                        -{discount}%
                      </div>
                    )}
                    {/* Progress bar at bottom of image */}
                    <div className="absolute bottom-0 left-0 right-0 px-2 pb-1.5 pt-1 bg-gradient-to-t from-black/30 to-transparent">
                      <div className="bg-white/40 rounded-full h-1.5">
                        <div className="bg-red-500 h-1.5 rounded-full transition-all" style={{ width: `${soldPct}%` }} />
                      </div>
                    </div>
                  </div>

                  <div className="px-2 pt-1.5 pb-2">
                    <p className="text-xs text-gray-500 mb-1">Đã bán {product.stockSold || 0}</p>
                    <p className="text-red-600 font-bold text-sm leading-tight">
                      {(product.flashPrice || 0).toLocaleString('vi-VN')}đ
                    </p>
                    <p className="text-gray-400 text-xs line-through mb-2">
                      {(product.originalPrice || 0).toLocaleString('vi-VN')}đ
                    </p>
                    <button
                      disabled={isSoldOut}
                      onClick={e => handleAddToCart(product, e)}
                      className={`w-full py-1.5 rounded text-xs font-semibold transition ${
                        isSoldOut
                          ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                          : 'bg-red-600 text-white hover:bg-red-700'
                      }`}
                    >
                      {isSoldOut ? 'Hết hàng' : 'Mua ngay'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Right arrow */}
          {canScrollRight && (
            <button
              onClick={() => setScrollIndex(i => i + 1)}
              className="absolute right-0 top-1/2 -translate-y-1/2 z-10 bg-white shadow-lg rounded-full w-9 h-9 flex items-center justify-center hover:bg-gray-50 transition border border-gray-200"
            >
              <ChevronRight className="w-5 h-5 text-gray-700" />
            </button>
          )}
        </div>
      ) : (
        <div className="py-10 text-center">
          <Zap className="w-10 h-10 text-red-300 mx-auto mb-2" />
          {!activeFlashSale ? (
            <>
              <p className="text-gray-500 font-semibold text-base">Sắp diễn ra...</p>
              {selectedSession?.startTime && (
                <p className="text-sm text-gray-400 mt-1">
                  Bắt đầu lúc{' '}
                  {new Date(selectedSession.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                </p>
              )}
            </>
          ) : (
            <>
              <p className="text-gray-500 font-semibold text-base">Sắp diễn ra...</p>
              <p className="text-sm text-gray-400 mt-1">Sản phẩm sẽ sớm được cập nhật</p>
            </>
          )}
        </div>
      )}
    </div>
  );
}
