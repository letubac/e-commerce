import React from 'react';
import { ChevronRight } from 'lucide-react';

/**
 * SectionBlock - Block tiêu đề chung cho các section trên trang
 *
 * Props:
 *  - title       : string   - Tên section
 *  - icon        : ReactNode - Icon hiển thị bên trái tiêu đề (optional)
 *  - onViewAll   : function  - Handler khi click "Xem tất cả" (optional, nếu không có thì ẩn nút)
 *  - viewAllLabel: string   - Label của nút, mặc định "Xem tất cả"
 *  - gradient    : string   - Tailwind gradient class, mặc định xanh dương
 *  - borderColor : string   - Màu CSS border trái/phải/dưới, vd "#0ea5e9". Mặc định lấy từ gradient
 *  - children    : ReactNode - Nội dung bên trong block
 *  - className   : string   - Class bổ sung cho wrapper (optional)
 */

// Map gradient → border color tương ứng
const GRADIENT_BORDER_MAP = {
  'from-red-500 to-red-700':       '#ef4444',
  'from-red-600 to-red-500':       '#ef4444',
  'from-green-500 to-teal-600':    '#14b8a6',
  'from-cyan-500 to-blue-600':     '#06b6d4',
  'from-blue-500 to-blue-700':     '#3b82f6',
  'from-orange-500 to-orange-700': '#f97316',
  'from-purple-500 to-purple-700': '#a855f7',
  'from-yellow-400 to-orange-500': '#f59e0b',
};

export default function SectionBlock({
  title,
  icon,
  onViewAll,
  viewAllLabel = 'Xem tất cả',
  gradient = 'from-cyan-500 to-blue-600',
  borderColor,
  children,
  className = '',
}) {
  const border = borderColor || GRADIENT_BORDER_MAP[gradient] || '#06b6d4';

  return (
    <div
      className={`bg-white rounded-xl shadow-sm ${className}`}
      style={{ border: `2px solid ${border}` }}
    >
      {/* Header bar */}
      <div className={`bg-gradient-to-r ${gradient} px-4 py-3 flex items-center justify-between rounded-t-[10px]`}>
        <div className="flex items-center gap-2">
          {icon && <span className="flex-shrink-0">{icon}</span>}
          <h2 className="text-white font-bold text-lg leading-none">{title}</h2>
        </div>
        {onViewAll && (
          <button
            onClick={onViewAll}
            className="text-white text-sm font-medium flex items-center gap-0.5 hover:underline opacity-90 hover:opacity-100 transition"
          >
            {viewAllLabel}
            <ChevronRight className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        {children}
      </div>
    </div>
  );
}
