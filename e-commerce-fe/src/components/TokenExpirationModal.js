/**
 * author: LeTuBac
 */
import React from 'react';
import { AlertTriangle, X } from 'lucide-react';

function TokenExpirationModal({ isOpen, onLogout, onDismiss, timeRemaining }) {
  if (!isOpen) return null;

  const formatTime = (ms) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 animate-fadeIn">
        <div className="p-6">
          {/* Header */}
          <div className="flex items-start justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-yellow-100 flex items-center justify-center">
                <AlertTriangle className="text-yellow-600" size={24} />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900">
                  Phiên đăng nhập sắp hết hạn
                </h3>
                <p className="text-sm text-gray-500">
                  {timeRemaining && `Còn ${formatTime(timeRemaining)}`}
                </p>
              </div>
            </div>
            <button
              onClick={onDismiss}
              className="text-gray-400 hover:text-gray-600 transition"
            >
              <X size={20} />
            </button>
          </div>

          {/* Content */}
          <div className="mb-6">
            <p className="text-gray-600 mb-2">
              Phiên đăng nhập của bạn sắp hết hạn. Vui lòng chọn một trong các tùy chọn sau:
            </p>
            <ul className="text-sm text-gray-500 space-y-1 ml-4">
              <li>• <strong>Đăng xuất ngay:</strong> Đăng xuất và đăng nhập lại để tiếp tục sử dụng</li>
              <li>• <strong>Bỏ qua:</strong> Tiếp tục sử dụng nhưng sẽ bị yêu cầu đăng nhập lại khi thực hiện thao tác</li>
            </ul>
          </div>

          {/* Actions */}
          <div className="flex gap-3">
            <button
              onClick={onLogout}
              className="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium"
            >
              Đăng xuất ngay
            </button>
            <button
              onClick={onDismiss}
              className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium"
            >
              Bỏ qua
            </button>
          </div>

          {/* Warning */}
          <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
            <p className="text-xs text-yellow-800">
              ⚠️ Nếu bỏ qua, bạn có thể bị yêu cầu đăng nhập lại bất kỳ lúc nào khi token hết hạn.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default TokenExpirationModal;
