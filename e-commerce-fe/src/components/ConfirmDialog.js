/**
 * author: LeTuBac
 */
import React from 'react';
import { AlertTriangle, Trash2, Plus, Edit, CheckCircle, XCircle, Lock, Unlock, Zap, Power, PauseCircle } from 'lucide-react';

/**
 * ACTION_TYPES — các loại hành động được hỗ trợ
 * Truyền vào prop `action` để tự động render icon, màu sắc, tiêu đề phù hợp.
 */
export const ACTION_TYPES = {
  DELETE:     'DELETE',
  CREATE:     'CREATE',
  UPDATE:     'UPDATE',
  ACTIVATE:   'ACTIVATE',
  DEACTIVATE: 'DEACTIVATE',
  CANCEL:     'CANCEL',
  APPROVE:    'APPROVE',
  REJECT:     'REJECT',
  LOCK:       'LOCK',
  UNLOCK:     'UNLOCK',
  PAUSE:      'PAUSE',
  STATUS_CHANGE: 'STATUS_CHANGE',
};

const CONFIG = {
  DELETE: {
    icon: Trash2,
    iconColor: 'text-red-500',
    iconBg: 'bg-red-100',
    confirmBtn: 'bg-red-600 hover:bg-red-700 text-white',
    confirmLabel: 'Xóa',
    title: (target) => `Xác nhận xóa ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn xóa ${target || 'mục này'}? Hành động này không thể hoàn tác.`,
  },
  CREATE: {
    icon: Plus,
    iconColor: 'text-green-500',
    iconBg: 'bg-green-100',
    confirmBtn: 'bg-green-600 hover:bg-green-700 text-white',
    confirmLabel: 'Tạo mới',
    title: (target) => `Xác nhận tạo ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn tạo mới ${target || 'này'}?`,
  },
  UPDATE: {
    icon: Edit,
    iconColor: 'text-blue-500',
    iconBg: 'bg-blue-100',
    confirmBtn: 'bg-blue-600 hover:bg-blue-700 text-white',
    confirmLabel: 'Cập nhật',
    title: (target) => `Xác nhận cập nhật ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn cập nhật ${target || 'này'}?`,
  },
  ACTIVATE: {
    icon: CheckCircle,
    iconColor: 'text-green-500',
    iconBg: 'bg-green-100',
    confirmBtn: 'bg-green-600 hover:bg-green-700 text-white',
    confirmLabel: 'Kích hoạt',
    title: (target) => `Xác nhận kích hoạt ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn kích hoạt ${target || 'này'}?`,
  },
  DEACTIVATE: {
    icon: XCircle,
    iconColor: 'text-orange-500',
    iconBg: 'bg-orange-100',
    confirmBtn: 'bg-orange-600 hover:bg-orange-700 text-white',
    confirmLabel: 'Vô hiệu hóa',
    title: (target) => `Xác nhận vô hiệu hóa ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn vô hiệu hóa ${target || 'này'}?`,
  },
  CANCEL: {
    icon: XCircle,
    iconColor: 'text-red-500',
    iconBg: 'bg-red-100',
    confirmBtn: 'bg-red-600 hover:bg-red-700 text-white',
    confirmLabel: 'Hủy',
    title: (target) => `Xác nhận hủy ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn hủy ${target || 'này'}? Hành động này không thể hoàn tác.`,
  },
  APPROVE: {
    icon: CheckCircle,
    iconColor: 'text-green-500',
    iconBg: 'bg-green-100',
    confirmBtn: 'bg-green-600 hover:bg-green-700 text-white',
    confirmLabel: 'Duyệt',
    title: (target) => `Xác nhận duyệt ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn duyệt ${target || 'này'}?`,
  },
  REJECT: {
    icon: XCircle,
    iconColor: 'text-red-500',
    iconBg: 'bg-red-100',
    confirmBtn: 'bg-red-600 hover:bg-red-700 text-white',
    confirmLabel: 'Từ chối',
    title: (target) => `Xác nhận từ chối ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn từ chối ${target || 'này'}?`,
  },
  LOCK: {
    icon: Lock,
    iconColor: 'text-yellow-500',
    iconBg: 'bg-yellow-100',
    confirmBtn: 'bg-yellow-600 hover:bg-yellow-700 text-white',
    confirmLabel: 'Khóa',
    title: (target) => `Xác nhận khóa ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn khóa ${target || 'này'}?`,
  },
  UNLOCK: {
    icon: Unlock,
    iconColor: 'text-green-500',
    iconBg: 'bg-green-100',
    confirmBtn: 'bg-green-600 hover:bg-green-700 text-white',
    confirmLabel: 'Mở khóa',
    title: (target) => `Xác nhận mở khóa ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn mở khóa ${target || 'này'}?`,
  },
  PAUSE: {
    icon: PauseCircle,
    iconColor: 'text-purple-500',
    iconBg: 'bg-purple-100',
    confirmBtn: 'bg-purple-600 hover:bg-purple-700 text-white',
    confirmLabel: 'Tạm ngưng',
    title: (target) => `Xác nhận tạm ngưng ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn tạm ngưng ${target || 'này'}?`,
  },
  STATUS_CHANGE: {
    icon: Zap,
    iconColor: 'text-indigo-500',
    iconBg: 'bg-indigo-100',
    confirmBtn: 'bg-indigo-600 hover:bg-indigo-700 text-white',
    confirmLabel: 'Cập nhật trạng thái',
    title: (target) => `Xác nhận cập nhật trạng thái ${target || ''}`,
    message: (target, detail) =>
      detail || `Bạn có chắc chắn muốn cập nhật trạng thái ${target || 'này'}?`,
  },
};

/**
 * ConfirmDialog — popup xác nhận trung tâm màn hình
 *
 * Props:
 *   isOpen      {boolean}  — có hiển thị popup không
 *   action      {string}   — một trong ACTION_TYPES (VD: 'DELETE', 'UPDATE', ...)
 *   target      {string}   — tên đối tượng (VD: 'sản phẩm', 'đơn hàng #123', 'người dùng')
 *   detail      {string}   — (tuỳ chọn) message custom thay thế message mặc định
 *   confirmLabel {string}  — (tuỳ chọn) override nút confirm
 *   onConfirm   {function} — callback khi ấn xác nhận
 *   onCancel    {function} — callback khi ấn hủy
 *   loading     {boolean}  — disable nút khi đang xử lý
 */
function ConfirmDialog({
  isOpen,
  action = ACTION_TYPES.UPDATE,
  target = '',
  detail = '',
  confirmLabel,
  onConfirm,
  onCancel,
  loading = false,
}) {
  if (!isOpen) return null;

  const cfg = CONFIG[action] || CONFIG.UPDATE;
  const Icon = cfg.icon;

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget && !loading) onCancel?.();
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={handleBackdropClick}
    >
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 animate-fade-in">
        {/* Icon */}
        <div className="flex items-center justify-center mb-4">
          <div className={`w-16 h-16 rounded-full flex items-center justify-center ${cfg.iconBg}`}>
            <Icon className={`w-8 h-8 ${cfg.iconColor}`} />
          </div>
        </div>

        {/* Title */}
        <h3 className="text-lg font-bold text-gray-900 text-center mb-2">
          {cfg.title(target)}
        </h3>

        {/* Message */}
        <p className="text-sm text-gray-500 text-center mb-6 leading-relaxed">
          {cfg.message(target, detail)}
        </p>

        {/* Buttons */}
        <div className="flex gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-xl font-medium hover:bg-gray-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className={`flex-1 px-4 py-2.5 rounded-xl font-medium transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 ${cfg.confirmBtn}`}
          >
            {loading ? (
              <>
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                </svg>
                Đang xử lý...
              </>
            ) : (
              confirmLabel || cfg.confirmLabel
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmDialog;
