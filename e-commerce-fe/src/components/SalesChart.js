/**
 * author: LeTuBac
 */
import React from 'react';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';

/**
 * Professional Sales Chart Component
 * Displays sales trends over time
 */
function SalesChart({ data, type = 'area' }) {
  // Convert data based on its structure
  // BE returns: { salesByDay: { "2024-01-01": 5000000, "2024-01-02": 6000000 } }
  let chartArray = [];
  
  if (Array.isArray(data)) {
    // If data is already array
    chartArray = data;
  } else if (data?.salesByDay && typeof data.salesByDay === 'object') {
    // If data is { salesByDay: {...} }
    chartArray = Object.entries(data.salesByDay).map(([date, revenue]) => ({
      date,
      revenue: Number(revenue) || 0
    }));
  } else if (typeof data === 'object' && data !== null) {
    // If data is directly the Map object
    chartArray = Object.entries(data).map(([date, revenue]) => ({
      date,
      revenue: Number(revenue) || 0
    }));
  }
  
  if (!chartArray || chartArray.length === 0) {
    return (
      <div className="flex items-center justify-center h-80 text-gray-500">
        <p>Không có dữ liệu để hiển thị</p>
      </div>
    );
  }

  // Format data for chart
  const chartData = chartArray.map((item) => ({
    date: formatDate(item.date),
    revenue: Number(item.revenue || 0),
    orders: Number(item.orders || 0)
  }));

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white p-3 border border-gray-300 rounded shadow-lg">
          <p className="text-sm font-medium text-gray-900">{payload[0].payload.date}</p>
          {payload.map((entry, index) => (
            <p key={index} style={{ color: entry.color }} className="text-sm">
              {entry.name}: {formatCurrency(entry.value)}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full h-80">
      <ResponsiveContainer width="100%" height="100%">
        {type === 'area' ? (
          <AreaChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis
              dataKey="date"
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
              tick={{ fill: '#6b7280' }}
            />
            <YAxis
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
              tick={{ fill: '#6b7280' }}
              tickFormatter={(value) => `${(value / 1000000).toFixed(0)}M`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{ paddingTop: '20px' }}
              iconType="line"
              contentStyle={{ color: '#6b7280', fontSize: '12px' }}
            />
            <Area
              type="monotone"
              dataKey="revenue"
              stroke="#3b82f6"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#colorRevenue)"
              name="Doanh thu (đ)"
              isAnimationActive={true}
            />
          </AreaChart>
        ) : (
          <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis
              dataKey="date"
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
              tick={{ fill: '#6b7280' }}
            />
            <YAxis
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
              tick={{ fill: '#6b7280' }}
              tickFormatter={(value) => `${(value / 1000000).toFixed(0)}M`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{ paddingTop: '20px' }}
              contentStyle={{ color: '#6b7280', fontSize: '12px' }}
            />
            <Line
              type="monotone"
              dataKey="revenue"
              stroke="#3b82f6"
              strokeWidth={2}
              dot={{ fill: '#3b82f6', r: 4 }}
              activeDot={{ r: 6 }}
              name="Doanh thu (đ)"
              isAnimationActive={true}
            />
            <Line
              type="monotone"
              dataKey="orders"
              stroke="#10b981"
              strokeWidth={2}
              dot={{ fill: '#10b981', r: 4 }}
              activeDot={{ r: 6 }}
              name="Đơn hàng"
              isAnimationActive={true}
            />
          </LineChart>
        )}
      </ResponsiveContainer>
    </div>
  );
}

// Helper function to format currency
function formatCurrency(value) {
  if (!value) return '0 đ';
  return `${Number(value).toLocaleString('vi-VN')} đ`;
}

// Helper function to format date
function formatDate(dateStr) {
  if (!dateStr) return '';
  
  try {
    const date = new Date(dateStr);
    return new Intl.DateTimeFormat('vi-VN', {
      month: 'short',
      day: 'numeric'
    }).format(date);
  } catch {
    return dateStr;
  }
}

export default SalesChart;
