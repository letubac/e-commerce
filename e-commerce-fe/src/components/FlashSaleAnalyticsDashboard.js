/**
 * author: LeTuBac
 * Flash Sale Analytics Dashboard — aggregate stats + per-sale breakdown
 */
import React, { useState, useEffect } from 'react';
import { TrendingUp, DollarSign, ShoppingBag, Package, BarChart2, Trophy, RefreshCw } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

const fmt = (n) => {
  const num = parseFloat(n);
  if (isNaN(num)) return '—';
  return num.toLocaleString('vi-VN') + '₫';
};

const fmtNum = (n) => {
  const num = parseInt(n);
  if (isNaN(num)) return '0';
  return num.toLocaleString('vi-VN');
};

function StatCard({ icon: Icon, label, value, sub, color }) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 p-5 flex items-start gap-4 shadow-sm">
      <div className={`p-3 rounded-xl ${color}`}>
        <Icon size={22} className="text-white" />
      </div>
      <div>
        <p className="text-xs text-gray-500 mb-0.5">{label}</p>
        <p className="text-2xl font-bold text-gray-800">{value}</p>
        {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

export default function FlashSaleAnalyticsDashboard({ onClose }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const loadedRef = React.useRef(false);

  const load = async () => {
    if (loadedRef.current) return;
    loadedRef.current = true;
    try {
      setLoading(true);
      const result = await adminApi.getFlashSaleDashboardAnalytics();
      setData(result);
    } catch (err) {
      // Session expired → adminApi already dispatched session-expired event
      // which shows "Phiên đăng nhập đã hết hạn" and redirects to login.
      // Only show analytics-specific error for non-auth failures.
      if (!err?.message?.includes('hết hạn')) {
        toast.error('Lỗi khi tải analytics');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadedRef.current = false;
    load();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="inline-block animate-spin rounded-full h-10 w-10 border-b-2 border-red-600" />
      </div>
    );
  }

  if (!data) return null;

  const rows = data.perFlashSale || [];
  // Sort by revenue desc
  const sorted = [...rows].sort((a, b) =>
    (parseFloat(b.totalRevenue) || 0) - (parseFloat(a.totalRevenue) || 0)
  );

  const maxRevenue = sorted.length > 0 ? parseFloat(sorted[0].totalRevenue) || 1 : 1;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <BarChart2 size={24} className="text-red-600" />
          Analytics Flash Sale
        </h2>
        <button
          onClick={load}
          className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700 transition"
        >
          <RefreshCw size={15} />
          Làm mới
        </button>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={TrendingUp}
          label="Tổng Flash Sale"
          value={fmtNum(data.totalFlashSales)}
          color="bg-red-500"
        />
        <StatCard
          icon={DollarSign}
          label="Tổng doanh thu"
          value={fmt(data.totalRevenue)}
          sub="Tính từ flash price × sold"
          color="bg-emerald-500"
        />
        <StatCard
          icon={ShoppingBag}
          label="Tổng lượt bán"
          value={fmtNum(data.totalSold)}
          sub="Các sản phẩm flash sale"
          color="bg-blue-500"
        />
        <StatCard
          icon={Package}
          label="Tổng sản phẩm"
          value={fmtNum(data.totalProducts)}
          sub="Trên tất cả Flash Sale"
          color="bg-purple-500"
        />
      </div>

      {/* Top sale banner */}
      {data.topSaleName && (
        <div className="bg-gradient-to-r from-amber-50 to-yellow-50 border border-amber-200 rounded-xl p-4 flex items-center gap-3">
          <Trophy size={24} className="text-amber-500 flex-shrink-0" />
          <div>
            <p className="text-sm font-semibold text-amber-800">Flash Sale doanh thu cao nhất</p>
            <p className="text-base font-bold text-amber-900">{data.topSaleName}</p>
            <p className="text-sm text-amber-700">{fmt(data.topSaleRevenue)}</p>
          </div>
        </div>
      )}

      {/* Per-flash-sale breakdown */}
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="px-5 py-3 border-b border-gray-100 bg-gray-50">
          <h3 className="text-sm font-semibold text-gray-700">Doanh thu theo từng Flash Sale</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="text-xs text-gray-500 uppercase border-b border-gray-100">
                <th className="px-5 py-3 text-left">Tên</th>
                <th className="px-5 py-3 text-right">Doanh thu</th>
                <th className="px-5 py-3 text-right">Bán được</th>
                <th className="px-5 py-3 text-right">SP</th>
                <th className="px-5 py-3 text-left w-40">Biểu đồ</th>
                <th className="px-5 py-3 text-center">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {sorted.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-10 text-gray-400">Chưa có dữ liệu</td>
                </tr>
              ) : sorted.map(row => {
                const revenue = parseFloat(row.totalRevenue) || 0;
                const barPct = maxRevenue > 0 ? Math.round((revenue / maxRevenue) * 100) : 0;
                const now = new Date();
                const end = new Date(row.endTime);
                const start = new Date(row.startTime);
                let badge, badgeClass;
                if (row.isActive && start <= now && end > now) {
                  badge = 'Đang chạy'; badgeClass = 'bg-green-100 text-green-700';
                } else if (!row.isActive && end <= now) {
                  badge = 'Đã kết thúc'; badgeClass = 'bg-gray-100 text-gray-500';
                } else if (start > now) {
                  badge = 'Sắp tới'; badgeClass = 'bg-blue-100 text-blue-700';
                } else {
                  badge = 'Đã tắt'; badgeClass = 'bg-orange-100 text-orange-600';
                }
                return (
                  <tr key={row.id} className="hover:bg-gray-50 transition">
                    <td className="px-5 py-3 font-medium text-gray-800 max-w-[180px] truncate">{row.name}</td>
                    <td className="px-5 py-3 text-right text-emerald-700 font-semibold">{fmt(row.totalRevenue)}</td>
                    <td className="px-5 py-3 text-right text-gray-600">{fmtNum(row.totalSales)}</td>
                    <td className="px-5 py-3 text-right text-gray-600">{row.totalProducts}</td>
                    <td className="px-5 py-3">
                      <div className="w-full bg-gray-100 rounded-full h-2">
                        <div
                          className="bg-gradient-to-r from-red-500 to-orange-400 h-2 rounded-full transition-all"
                          style={{ width: `${barPct}%` }}
                        />
                      </div>
                    </td>
                    <td className="px-5 py-3 text-center">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${badgeClass}`}>{badge}</span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
