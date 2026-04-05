/**
 * author: LeTuBac
 * Reusable confirmation dialog — replaces window.confirm()
 *
 * Usage:
 *   const { confirmModal, showConfirm } = useConfirm();
 *   // then in JSX: {confirmModal}
 *   // trigger: await showConfirm({ title, message, variant })
 */
import React, { useState, useCallback } from 'react';
import { AlertTriangle, Trash2, PowerOff, Info } from 'lucide-react';

// ─── Standalone modal component ──────────────────────────────────────────────

export default function ConfirmModal({
  isOpen,
  title = 'Xác nhận',
  message,
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  variant = 'warning', // 'warning' | 'danger' | 'info'
  onConfirm,
  onCancel,
}) {
  if (!isOpen) return null;

  const variantConfig = {
    warning: {
      icon: AlertTriangle,
      iconBg: 'bg-amber-100',
      iconColor: 'text-amber-600',
      btnClass: 'bg-amber-600 hover:bg-amber-700 text-white',
    },
    danger: {
      icon: Trash2,
      iconBg: 'bg-red-100',
      iconColor: 'text-red-600',
      btnClass: 'bg-red-600 hover:bg-red-700 text-white',
    },
    deactivate: {
      icon: PowerOff,
      iconBg: 'bg-orange-100',
      iconColor: 'text-orange-600',
      btnClass: 'bg-orange-600 hover:bg-orange-700 text-white',
    },
    info: {
      icon: Info,
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600',
      btnClass: 'bg-blue-600 hover:bg-blue-700 text-white',
    },
  };

  const cfg = variantConfig[variant] || variantConfig.warning;
  const Icon = cfg.icon;

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-[9999] p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onCancel?.(); }}
    >
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm animate-in fade-in zoom-in-95 duration-200">
        {/* Icon */}
        <div className="flex flex-col items-center px-8 pt-8 pb-4">
          <div className={`w-16 h-16 rounded-full ${cfg.iconBg} flex items-center justify-center mb-4`}>
            <Icon size={32} className={cfg.iconColor} />
          </div>
          <h3 className="text-lg font-bold text-gray-800 text-center">{title}</h3>
          {message && (
            <p className="mt-2 text-sm text-gray-600 text-center whitespace-pre-line leading-relaxed">
              {message}
            </p>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-3 px-8 pb-8">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition"
          >
            {cancelText}
          </button>
          <button
            onClick={onConfirm}
            className={`flex-1 px-4 py-2.5 rounded-xl text-sm font-medium transition ${cfg.btnClass}`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Hook: useConfirm ─────────────────────────────────────────────────────────

export function useConfirm() {
  const [state, setState] = useState({ isOpen: false, resolve: null, props: {} });

  const showConfirm = useCallback((props) => {
    return new Promise((resolve) => {
      setState({ isOpen: true, resolve, props });
    });
  }, []);

  const handleConfirm = useCallback(() => {
    setState((s) => { s.resolve?.(true); return { isOpen: false, resolve: null, props: {} }; });
  }, []);

  const handleCancel = useCallback(() => {
    setState((s) => { s.resolve?.(false); return { isOpen: false, resolve: null, props: {} }; });
  }, []);

  const confirmModal = (
    <ConfirmModal
      isOpen={state.isOpen}
      {...state.props}
      onConfirm={handleConfirm}
      onCancel={handleCancel}
    />
  );

  return { showConfirm, confirmModal };
}
