import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Smartphone, Laptop, Monitor, Headphones, Watch, Camera, Gamepad2, Tv, Home, Zap, Package, ChevronLeft, ChevronRight, Star, Shield, RefreshCw, CreditCard, Truck } from 'lucide-react';
import FlashSale from '../components/FlashSale';
import NewArrivals from '../components/NewArrivals';
import SectionBlock from '../components/SectionBlock';
import TrendingSearch from '../components/TrendingSearch';
import WelcomePopup from '../components/WelcomePopup';
import { useAuth } from '../context/AuthContext';
import api, { getImageUrl } from '../api/api';

const categoryIcons = {
  'Điện thoại': Smartphone,
  'Laptop': Laptop,
  'Tablet': Monitor,
  'Đồng hồ thông minh': Watch,
  'Đồng hồ': Watch,
  'Tai nghe': Headphones,
  'Máy ảnh': Camera,
  'TV': Tv,
  'Màn hình': Monitor,
  'Gaming': Gamepad2,
  'Quần áo': Home,
  'Thời trang': Home,
};

const HERO_SLIDES = [
  {
    gradient: 'from-blue-600 via-blue-500 to-purple-600',
    title: 'Flash Sale hôm nay',
    subtitle: 'Giảm đến 70% sản phẩm công nghệ',
    cta: 'Mua ngay',
    ctaLink: '/flash-sale',
    badge: '🔥 HOT DEAL',
  },
  {
    gradient: 'from-emerald-500 via-green-500 to-teal-500',
    title: 'Hàng mới về',
    subtitle: 'Sản phẩm hot nhất tháng này',
    cta: 'Khám phá',
    ctaLink: '/products',
    badge: '✨ MỚI',
  },
  {
    gradient: 'from-orange-500 via-orange-400 to-yellow-500',
    title: 'Free ship toàn quốc',
    subtitle: 'Đơn hàng từ 200.000đ - Giao siêu tốc',
    cta: 'Đặt hàng ngay',
    ctaLink: '/products',
    badge: '🚚 MIỄN PHÍ',
  },
];

function HomePage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [showWelcomePopup, setShowWelcomePopup] = useState(false);
  const [hasShownPopup, setHasShownPopup] = useState(false);
  const [categories, setCategories] = useState([]);
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [showAllCats, setShowAllCats] = useState(false);
  const CATS_VISIBLE = 10;

  useEffect(() => {
    if (user && !hasShownPopup) {
      setShowWelcomePopup(true);
      setHasShownPopup(true);
    }
  }, [user, hasShownPopup]);

  // Auto-play banner
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide(prev => (prev + 1) % HERO_SLIDES.length);
    }, 4000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function fetchData() {
      try {
        setLoadingCategories(true);
        const catData = await api.getCategories().catch(() => []);
        if (!cancelled) {
          const cats = Array.isArray(catData) ? catData : (catData?.content || []);
          setCategories(cats.slice(0, 10));
        }
      } catch (e) {
        if (!cancelled) setCategories([]);
      } finally {
        if (!cancelled) setLoadingCategories(false);
      }

      try {
        setLoadingProducts(true);
        const prodData = await api.getFeaturedProducts().catch(() => []);
        if (!cancelled) {
          const prods = Array.isArray(prodData) ? prodData : (prodData?.content || prodData?.items || []);
          setFeaturedProducts(prods.slice(0, 8));
        }
      } catch (e) {
        if (!cancelled) setFeaturedProducts([]);
      } finally {
        if (!cancelled) setLoadingProducts(false);
      }
    }
    fetchData();
    return () => { cancelled = true; };
  }, []);

  const prevSlide = () => setCurrentSlide(prev => (prev - 1 + HERO_SLIDES.length) % HERO_SLIDES.length);
  const nextSlide = () => setCurrentSlide(prev => (prev + 1) % HERO_SLIDES.length);

  return (
    <div className="min-h-screen bg-gray-100">
      <WelcomePopup
        isOpen={showWelcomePopup}
        onClose={() => setShowWelcomePopup(false)}
        userName={user?.fullName || user?.username}
      />

      {/* 1. Hero Banner Slider */}
      <section className="relative overflow-hidden" style={{ height: '320px' }}>
        {HERO_SLIDES.map((slide, i) => (
          <div
            key={i}
            className={`absolute inset-0 bg-gradient-to-r ${slide.gradient} transition-opacity duration-700 ${i === currentSlide ? 'opacity-100' : 'opacity-0'}`}
          >
            <div className="max-w-7xl mx-auto px-6 h-full flex items-center">
              <div className="text-white max-w-lg">
                <div className="inline-block bg-white/20 backdrop-blur-sm text-white text-xs font-bold px-3 py-1 rounded-full mb-4">
                  {slide.badge}
                </div>
                <h1 className="text-4xl md:text-5xl font-bold mb-3 leading-tight">{slide.title}</h1>
                <p className="text-lg md:text-xl text-white/90 mb-6">{slide.subtitle}</p>
                <button
                  onClick={() => navigate(slide.ctaLink)}
                  className="bg-white text-gray-800 font-bold px-8 py-3 rounded-xl hover:bg-gray-100 transition shadow-lg text-base"
                >
                  {slide.cta}
                </button>
              </div>
            </div>
          </div>
        ))}

        {/* Arrows */}
        <button
          onClick={prevSlide}
          className="absolute left-4 top-1/2 -translate-y-1/2 bg-white/30 hover:bg-white/50 text-white rounded-full w-10 h-10 flex items-center justify-center transition backdrop-blur-sm"
        >
          <ChevronLeft className="w-6 h-6" />
        </button>
        <button
          onClick={nextSlide}
          className="absolute right-4 top-1/2 -translate-y-1/2 bg-white/30 hover:bg-white/50 text-white rounded-full w-10 h-10 flex items-center justify-center transition backdrop-blur-sm"
        >
          <ChevronRight className="w-6 h-6" />
        </button>

        {/* Dot indicators */}
        <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
          {HERO_SLIDES.map((_, i) => (
            <button
              key={i}
              onClick={() => setCurrentSlide(i)}
              className={`rounded-full transition-all ${i === currentSlide ? 'w-6 h-2.5 bg-white' : 'w-2.5 h-2.5 bg-white/50'}`}
            />
          ))}
        </div>
      </section>

      {/* 2. Quick Links - Category icons (sticky below header) */}
      <section className="bg-white shadow-sm sticky top-16 z-40">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex flex-wrap justify-center gap-1">
            {loadingCategories
              ? [...Array(8)].map((_, i) => (
                  <div key={i} className="flex-shrink-0 w-20 h-20 bg-gray-100 rounded-xl animate-pulse" />
                ))
              : (showAllCats ? categories : categories.slice(0, CATS_VISIBLE)).map((cat) => {
                  const Icon = categoryIcons[cat.name] || Package;
                  return (
                    <button
                      key={cat.id}
                      onClick={() => navigate(`/products?categoryId=${cat.id}`)}
                      className="flex flex-col items-center gap-1.5 px-3 py-2 rounded-xl hover:bg-red-50 transition group min-w-[72px]"
                    >
                      <div className="w-12 h-12 bg-red-50 group-hover:bg-red-100 rounded-xl flex items-center justify-center transition">
                        <Icon className="w-6 h-6 text-red-600" />
                      </div>
                      <span className="text-xs text-gray-600 group-hover:text-red-600 font-medium text-center leading-tight transition line-clamp-2">
                        {cat.name}
                      </span>
                    </button>
                  );
                })}
            {!loadingCategories && categories.length > CATS_VISIBLE && (
              <button
                onClick={() => setShowAllCats(prev => !prev)}
                className="flex flex-col items-center gap-1.5 px-3 py-2 rounded-xl hover:bg-gray-50 transition min-w-[72px]"
              >
                <div className="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center">
                  <ChevronRight className={`w-6 h-6 text-gray-500 transition-transform ${showAllCats ? 'rotate-90' : ''}`} />
                </div>
                <span className="text-xs text-gray-500 font-medium text-center">
                  {showAllCats ? 'Thu gọn' : 'Xem thêm'}
                </span>
              </button>
            )}
          </div>
        </div>
      </section>

      {/* 3. Flash Sale */}
      <section className="max-w-7xl mx-auto px-4 pt-4">
        <FlashSale />
      </section>

      {/* 4. Featured Products */}
      <section className="max-w-7xl mx-auto px-4 py-6">
        <SectionBlock
          title="Sản phẩm nổi bật"
          icon={<Zap className="w-5 h-5 text-yellow-300 fill-yellow-300" />}
          onViewAll={() => navigate('/products')}
          gradient="from-red-500 to-red-700"
        >

          {loadingProducts ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
              {[...Array(8)].map((_, i) => (
                <div key={i} className="bg-gray-100 rounded-lg animate-pulse h-60" />
              ))}
            </div>
          ) : featuredProducts.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
              {featuredProducts.map((product) => {
                const price = (product.salePrice || product.price || 0);
                const originalPrice = product.price || 0;
                const showStrike = product.salePrice && product.salePrice < originalPrice;
                const primaryImage = product.productImages?.find(img => img.primary) || product.productImages?.[0];
                const imgUrl = getImageUrl(primaryImage?.imageUrl || product.imageUrl);
                return (
                  <div
                    key={product.id}
                    onClick={() => navigate(`/product/${product.id}`)}
                    className="border border-gray-100 rounded-lg overflow-hidden hover:shadow-md transition cursor-pointer group"
                  >
                    <div className="relative overflow-hidden bg-gray-50" style={{ height: '180px' }}>
                      {imgUrl ? (
                        <img
                          src={imgUrl}
                          alt={product.name}
                          className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                          onError={e => { e.target.style.display = 'none'; }}
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-300">
                          <Package className="w-12 h-12" />
                        </div>
                      )}
                      {showStrike && (
                        <div className="absolute top-2 left-2 bg-red-500 text-white text-xs font-bold px-1.5 py-0.5 rounded">
                          -{Math.round((1 - product.salePrice / originalPrice) * 100)}%
                        </div>
                      )}
                    </div>
                    <div className="p-3">
                      <p className="text-sm text-gray-700 font-medium line-clamp-2 mb-1.5">{product.name}</p>
                      {product.rating > 0 && (
                        <div className="flex items-center gap-0.5 mb-1">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              className={`w-3 h-3 ${i < Math.round(product.rating) ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200'}`}
                            />
                          ))}
                          <span className="text-xs text-gray-400 ml-1">({product.rating.toFixed(1)})</span>
                        </div>
                      )}
                      <p className="text-red-600 font-bold">
                        {price.toLocaleString('vi-VN')}đ
                      </p>
                      {showStrike && (
                        <p className="text-gray-400 text-xs line-through">
                          {originalPrice.toLocaleString('vi-VN')}đ
                        </p>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-12 text-gray-400">
              <Package className="w-12 h-12 mx-auto mb-2 opacity-40" />
              <p>Không có sản phẩm nổi bật</p>
            </div>
          )}

          {featuredProducts.length > 0 && (
            <div className="mt-5 text-center">
              <button
                onClick={() => navigate('/products')}
                className="bg-red-600 text-white px-8 py-2.5 rounded-xl font-semibold hover:bg-red-700 transition"
              >
                Xem tất cả sản phẩm
              </button>
            </div>
          )}
        </SectionBlock>
      </section>

      {/* 5. Promotion Banners */}
      <section className="max-w-7xl mx-auto px-4 pb-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div
            onClick={() => navigate('/products')}
            className="bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl p-6 cursor-pointer hover:shadow-lg transition group"
          >
            <div className="flex items-center gap-4">
              <div className="bg-white/20 rounded-xl p-3 group-hover:bg-white/30 transition">
                <Shield className="w-8 h-8 text-white" />
              </div>
              <div className="text-white">
                <h3 className="font-bold text-lg">Bảo hành chính hãng</h3>
                <p className="text-blue-100 text-sm">12-24 tháng bảo hành tại nhà</p>
              </div>
            </div>
          </div>
          <div
            onClick={() => navigate('/products')}
            className="bg-gradient-to-r from-emerald-500 to-green-600 rounded-xl p-6 cursor-pointer hover:shadow-lg transition group"
          >
            <div className="flex items-center gap-4">
              <div className="bg-white/20 rounded-xl p-3 group-hover:bg-white/30 transition">
                <RefreshCw className="w-8 h-8 text-white" />
              </div>
              <div className="text-white">
                <h3 className="font-bold text-lg">Đổi trả 30 ngày</h3>
                <p className="text-green-100 text-sm">Hoàn tiền 100% nếu không hài lòng</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 6. New Arrivals */}
      <section className="bg-white py-6">
        <div className="max-w-7xl mx-auto px-4">
          <NewArrivals />
        </div>
      </section>

      {/* 7. Trust Badges */}
      <section className="max-w-7xl mx-auto px-4 py-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { icon: Truck, title: 'Giao hàng nhanh', sub: 'Trong vòng 24 giờ', color: 'text-blue-600 bg-blue-50' },
            { icon: Shield, title: 'Hàng chính hãng', sub: '100% có nguồn gốc rõ ràng', color: 'text-green-600 bg-green-50' },
            { icon: CreditCard, title: 'Thanh toán an toàn', sub: 'Bảo mật SSL/TLS', color: 'text-purple-600 bg-purple-50' },
            { icon: RefreshCw, title: 'Đổi trả dễ dàng', sub: 'Trong vòng 30 ngày', color: 'text-orange-600 bg-orange-50' },
          ].map(({ icon: Icon, title, sub, color }) => (
            <div key={title} className="bg-white rounded-xl p-4 shadow-sm flex items-center gap-3">
              <div className={`${color} rounded-xl p-2.5 flex-shrink-0`}>
                <Icon className="w-6 h-6" />
              </div>
              <div>
                <p className="font-semibold text-gray-800 text-sm">{title}</p>
                <p className="text-gray-500 text-xs">{sub}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 8. Trending Search */}
      <section className="bg-white py-6">
        <div className="max-w-7xl mx-auto px-4">
          <TrendingSearch />
        </div>
      </section>
    </div>
  );
}

export default HomePage;
