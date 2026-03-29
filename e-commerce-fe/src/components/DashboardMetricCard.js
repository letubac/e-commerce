import React from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';

/**
 * Professional Metric Card Component
 * Displays key performance metrics with trend indicators
 */
function DashboardMetricCard({ 
  title, 
  value, 
  icon: Icon, 
  color = 'bg-blue-500',
  trend = null,
  trendIs = 'positive', // 'positive' or 'negative'
  unit = '',
  subtitle = null,
  onClick = null
}) {
  const isPositive = trendIs === 'positive';
  const TrendIcon = isPositive ? TrendingUp : TrendingDown;
  const trendColorClass = isPositive ? 'text-green-500' : 'text-red-500';
  const bgColorClass = isPositive ? 'bg-green-50' : 'bg-red-50';

  return (
    <div 
      onClick={onClick}
      className={`bg-white rounded-lg shadow-md hover:shadow-lg transition-all duration-300 p-6 border border-gray-100 ${onClick ? 'cursor-pointer' : ''}`}
    >
      <div className="flex items-start justify-between">
        {/* Left Side: Text Content */}
        <div className="flex-1">
          <p className="text-sm font-medium text-gray-600 mb-2">{title}</p>
          
          {/* Main Value */}
          <div className="flex items-baseline">
            <p className="text-3xl font-bold text-gray-900">
              {typeof value === 'number' ? value.toLocaleString('vi-VN') : value}
            </p>
            {unit && <span className="text-sm text-gray-500 ml-2">{unit}</span>}
          </div>

          {/* Trend Indicator */}
          {trend !== null && (
            <div className={`flex items-center mt-3 px-2 py-1 rounded ${bgColorClass} w-fit`}>
              <TrendIcon size={16} className={trendColorClass} />
              <span className={`text-xs font-medium ml-1 ${trendColorClass}`}>
                {isPositive ? '+' : ''}{trend}%
              </span>
            </div>
          )}

          {/* Subtitle */}
          {subtitle && (
            <p className="text-xs text-gray-500 mt-2">{subtitle}</p>
          )}
        </div>

        {/* Right Side: Icon */}
        {Icon && (
          <div className={`p-3 rounded-lg ${color} flex items-center justify-center`}>
            <Icon className="text-white" size={28} />
          </div>
        )}
      </div>
    </div>
  );
}

export default DashboardMetricCard;
