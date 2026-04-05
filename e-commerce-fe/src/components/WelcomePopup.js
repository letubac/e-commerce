import React, { useEffect, useRef, useState } from 'react';
import { X, Sparkles, Star, Heart, Award } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const WelcomePopup = ({ isOpen, onClose, userName }) => {
  const navigate = useNavigate();
  const [confetti, setConfetti] = useState([]);
  const [flowers, setFlowers] = useState([]);
  const onCloseRef = useRef(onClose);

  // Keep ref in sync without triggering effect
  useEffect(() => { onCloseRef.current = onClose; }, [onClose]);

  useEffect(() => {
    if (!isOpen) return;

    // Generate confetti particles
    const confettiArray = Array.from({ length: 50 }, (_, i) => ({
      id: i,
      left: Math.random() * 100,
      delay: Math.random() * 3,
      duration: 3 + Math.random() * 2,
      color: ['#ef4444', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ec4899'][Math.floor(Math.random() * 6)]
    }));
    setConfetti(confettiArray);

    // Generate floating flowers
    const flowersArray = Array.from({ length: 20 }, (_, i) => ({
      id: i,
      left: Math.random() * 100,
      delay: Math.random() * 2,
      duration: 4 + Math.random() * 2,
    }));
    setFlowers(flowersArray);

    // Auto close after 8 seconds
    const timer = setTimeout(() => {
      onCloseRef.current?.();
    }, 8000);

    return () => clearTimeout(timer);
  }, [isOpen]); // Only depends on isOpen — stable ref handles onClose

  if (!isOpen) return null;

  return (
    /* Single backdrop — clicking outside modal calls onClose */
    <div
      className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm z-50 flex items-center justify-center p-4 transition-opacity duration-300"
      onClick={onClose}
    >
      {/* Confetti Animation */}
      {confetti.map((particle) => (
        <div
          key={particle.id}
          className="absolute w-3 h-3 rounded-full animate-confetti pointer-events-none"
          style={{
            left: `${particle.left}%`,
            top: '-20px',
            backgroundColor: particle.color,
            animationDelay: `${particle.delay}s`,
            animationDuration: `${particle.duration}s`
          }}
        />
      ))}

      {/* Floating Flowers */}
      {flowers.map((flower) => (
        <div
          key={`flower-${flower.id}`}
          className="absolute animate-float opacity-70 pointer-events-none"
          style={{
            left: `${flower.left}%`,
            top: '-50px',
            animationDelay: `${flower.delay}s`,
            animationDuration: `${flower.duration}s`
          }}
        >
          <Sparkles className="text-yellow-300" size={24} />
        </div>
      ))}

      {/* Modal Content — stopPropagation so clicks inside don't close */}
      <div
        className="relative bg-gradient-to-br from-red-600 via-red-700 to-pink-600 rounded-3xl shadow-2xl max-w-md w-full p-5 transform animate-scale-in"
        onClick={(e) => e.stopPropagation()}
      >
          {/* Close Button */}
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-white hover:text-red-200 transition-colors duration-200 z-20"
          >
            <X size={28} />
          </button>

          {/* Decorative Stars */}
          <div className="absolute top-8 left-8 animate-spin-slow">
            <Star className="text-yellow-300 fill-yellow-300" size={32} />
          </div>
          <div className="absolute bottom-8 right-8 animate-spin-slow animation-delay-1000">
            <Star className="text-yellow-300 fill-yellow-300" size={28} />
          </div>
          <div className="absolute top-1/2 left-12 animate-pulse-glow">
            <Heart className="text-pink-300 fill-pink-300" size={24} />
          </div>
          <div className="absolute top-1/3 right-12 animate-bounce">
            <Sparkles className="text-yellow-200" size={28} />
          </div>

          {/* Content */}
          <div className="text-center relative z-10">
            {/* Award Icon */}
            <div className="mb-6 flex justify-center animate-bounce-slow">
              <div className="bg-white rounded-full p-6 shadow-lg">
                <Award className="text-red-600" size={64} />
              </div>
            </div>

            {/* Welcome Text */}
            <h1 className="text-4xl md:text-5xl font-bold text-white mb-4 animate-fade-in-up">
              🎉 Chào mừng bạn trở lại! 🎉
            </h1>
            
            {userName && (
              <p className="text-2xl md:text-3xl text-yellow-200 font-semibold mb-6 animate-fade-in-up animation-delay-200">
                {userName}
              </p>
            )}

            <p className="text-xl text-white mb-8 animate-fade-in-up animation-delay-400">
              Cảm ơn bạn đã tin tưởng và lựa chọn <span className="font-bold">E-SHOP</span>
              <br />
              <span className="text-yellow-200">✨ Nơi mua sắm công nghệ hàng đầu Việt Nam ✨</span>
            </p>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center animate-fade-in-up animation-delay-600">
              <button
                onClick={() => { navigate('/products'); onClose(); }}
                className="bg-white text-red-600 px-8 py-4 rounded-xl font-bold text-lg hover:bg-yellow-100 transition-all duration-300 transform hover:scale-105 shadow-lg"
              >
                🛍️ Khám phá ngay
              </button>
              <button
                onClick={() => { navigate('/flash-sale'); onClose(); }}
                className="bg-yellow-400 text-red-800 px-8 py-4 rounded-xl font-bold text-lg hover:bg-yellow-300 transition-all duration-300 transform hover:scale-105 shadow-lg"
              >
                ⚡ Flash Sale
              </button>
            </div>

            {/* Gift Message */}
            <div className="mt-8 bg-white bg-opacity-20 rounded-xl p-4 backdrop-blur-sm animate-fade-in-up animation-delay-800">
              <p className="text-white text-sm md:text-base">
                🎁 <span className="font-semibold">Ưu đãi đặc biệt</span> dành cho bạn: 
                <span className="text-yellow-200 font-bold"> Giảm 10% đơn hàng đầu tiên!</span>
              </p>
            </div>
          </div>
      </div>
    </div>
  );
};

export default WelcomePopup;
