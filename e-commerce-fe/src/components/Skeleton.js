import React from 'react';

/**
 * Base skeleton shimmer block.
 */
export function SkeletonBlock({ className = '' }) {
  return <div className={`bg-gray-200 animate-pulse rounded ${className}`} />;
}

/**
 * Skeleton for a single product card (matches ProductCard layout).
 */
export function ProductCardSkeleton() {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Image */}
      <SkeletonBlock className="w-full h-48" />
      <div className="p-4 space-y-3">
        {/* Title */}
        <SkeletonBlock className="h-4 w-4/5" />
        <SkeletonBlock className="h-4 w-3/5" />
        {/* Rating */}
        <SkeletonBlock className="h-3 w-2/5" />
        {/* Price */}
        <SkeletonBlock className="h-5 w-1/2" />
        {/* Button */}
        <SkeletonBlock className="h-9 w-full rounded-lg" />
      </div>
    </div>
  );
}

/**
 * Grid of product card skeletons.
 * @param {number} count - How many cards to render (default: 12)
 */
export function ProductGridSkeleton({ count = 12 }) {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
      {Array.from({ length: count }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  );
}

/**
 * Skeleton for a single order row in OrdersPage.
 */
export function OrderItemSkeleton() {
  return (
    <div className="bg-white rounded-lg shadow-sm p-4 mb-3 border border-gray-100">
      <div className="flex items-center space-x-4">
        <SkeletonBlock className="w-16 h-16 rounded-lg flex-shrink-0" />
        <div className="flex-1 space-y-2">
          <SkeletonBlock className="h-4 w-3/4" />
          <SkeletonBlock className="h-3 w-1/2" />
          <SkeletonBlock className="h-3 w-1/3" />
        </div>
        <div className="space-y-2 flex-shrink-0">
          <SkeletonBlock className="h-5 w-20" />
          <SkeletonBlock className="h-7 w-24 rounded-lg" />
        </div>
      </div>
    </div>
  );
}

/**
 * Skeleton for the product details page.
 */
export function ProductDetailSkeleton() {
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-sm p-6 animate-pulse">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Images */}
          <div>
            <SkeletonBlock className="h-96 w-full rounded-lg mb-4" />
            <div className="flex gap-2">
              {[...Array(4)].map((_, i) => (
                <SkeletonBlock key={i} className="h-20 w-20 rounded-lg" />
              ))}
            </div>
          </div>
          {/* Info */}
          <div className="space-y-4">
            <SkeletonBlock className="h-6 w-4/5" />
            <SkeletonBlock className="h-6 w-3/5" />
            <SkeletonBlock className="h-4 w-2/5" />
            <SkeletonBlock className="h-8 w-1/3" />
            <SkeletonBlock className="h-4 w-full" />
            <SkeletonBlock className="h-4 w-full" />
            <SkeletonBlock className="h-4 w-4/5" />
            <div className="flex gap-3 mt-6">
              <SkeletonBlock className="h-12 flex-1 rounded-lg" />
              <SkeletonBlock className="h-12 flex-1 rounded-lg" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * A simple inline text-line skeleton.
 */
export function SkeletonLine({ width = 'w-full', height = 'h-4' }) {
  return <SkeletonBlock className={`${width} ${height}`} />;
}
