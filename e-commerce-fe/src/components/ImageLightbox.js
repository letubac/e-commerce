import React, { useState, useEffect } from 'react';
import { X, ChevronLeft, ChevronRight, ZoomIn, ZoomOut } from 'lucide-react';
import './ImageLightbox.css';

const ImageLightbox = ({ images, initialIndex = 0, onClose }) => {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);
  const [isZoomed, setIsZoomed] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  // Handle keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        onClose();
      } else if (e.key === 'ArrowLeft') {
        handlePrevious();
      } else if (e.key === 'ArrowRight') {
        handleNext();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentIndex, onClose]);

  // Prevent body scroll when lightbox is open
  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, []);

  // Reset zoom and image loaded state when changing images
  useEffect(() => {
    setIsZoomed(false);
    setImageLoaded(false);
  }, [currentIndex]);

  const handleNext = () => {
    setCurrentIndex((prev) => (prev + 1) % images.length);
  };

  const handlePrevious = () => {
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);
  };

  const handleThumbnailClick = (index) => {
    setCurrentIndex(index);
  };

  const toggleZoom = () => {
    setIsZoomed(!isZoomed);
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  if (!images || images.length === 0) {
    return null;
  }

  const currentImage = images[currentIndex];

  return (
    <div className="lightbox-overlay" onClick={handleBackdropClick}>
      <div className="lightbox-container">
        {/* Header */}
        <div className="lightbox-header">
          <div className="lightbox-counter">
            {currentIndex + 1} / {images.length}
          </div>
          <div className="lightbox-controls">
            <button
              onClick={toggleZoom}
              className="lightbox-btn"
              title={isZoomed ? 'Thu nhỏ' : 'Phóng to'}
            >
              {isZoomed ? <ZoomOut size={20} /> : <ZoomIn size={20} />}
            </button>
            <button
              onClick={onClose}
              className="lightbox-btn"
              title="Đóng (ESC)"
            >
              <X size={24} />
            </button>
          </div>
        </div>

        {/* Main Image */}
        <div className="lightbox-main">
          {images.length > 1 && (
            <button
              onClick={handlePrevious}
              className="lightbox-nav lightbox-nav-left"
              title="Ảnh trước (←)"
            >
              <ChevronLeft size={32} />
            </button>
          )}

          <div className={`lightbox-image-container ${isZoomed ? 'zoomed' : ''}`}>
            {!imageLoaded && (
              <div className="lightbox-loading">
                <div className="spinner"></div>
                <p>Đang tải...</p>
              </div>
            )}
            <img
              src={currentImage}
              alt={`Product ${currentIndex + 1}`}
              className={`lightbox-image ${imageLoaded ? 'loaded' : ''}`}
              onLoad={() => setImageLoaded(true)}
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = '/images/placeholder.jpg';
                setImageLoaded(true);
              }}
            />
          </div>

          {images.length > 1 && (
            <button
              onClick={handleNext}
              className="lightbox-nav lightbox-nav-right"
              title="Ảnh tiếp theo (→)"
            >
              <ChevronRight size={32} />
            </button>
          )}
        </div>

        {/* Thumbnail Strip */}
        {images.length > 1 && (
          <div className="lightbox-thumbnails">
            <div className="lightbox-thumbnails-scroll">
              {images.map((image, index) => (
                <button
                  key={index}
                  onClick={() => handleThumbnailClick(index)}
                  className={`lightbox-thumbnail ${
                    index === currentIndex ? 'active' : ''
                  }`}
                >
                  <img
                    src={image}
                    alt={`Thumbnail ${index + 1}`}
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = '/images/placeholder.jpg';
                    }}
                  />
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ImageLightbox;
