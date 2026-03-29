import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell
} from 'recharts';

/**
 * User Growth Chart Component
 * Shows user statistics and trends
 */
function UserGrowthChart({ data }) {
  // Ensure data is object
  const validData = (typeof data === 'object' && data !== null) ? data : {};
  
  const chartData = [
    {
      name: 'Tổng người dùng',
      value: validData.totalUsers || 0,
      color: '#8b5cf6'
    },
    {
      name: 'Người dùng hoạt động',
      value: validData.activeUsers || 0,
      color: '#6366f1'
    },
    {
      name: 'Người dùng mới',
      value: validData.newUsersThisMonth || 0,
      color: '#3b82f6'
    },
    {
      name: 'Người dùng trở lại',
      value: validData.returningUsers || 0,
      color: '#0ea5e9'
    }
  ];

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white p-3 border border-gray-300 rounded shadow-lg">
          <p style={{ color: payload[0].fill }} className="text-sm font-medium">
            {payload[0].payload.name}
          </p>
          <p className="text-sm text-gray-600">{payload[0].value} người</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full h-80">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart
          data={chartData}
          margin={{ top: 10, right: 30, left: 0, bottom: 0 }}
          layout="vertical"
        >
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis
            type="number"
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
            tick={{ fill: '#6b7280' }}
          />
          <YAxis
            dataKey="name"
            type="category"
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
            tick={{ fill: '#6b7280' }}
            width={120}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="value" fill="#8884d8" radius={[0, 8, 8, 0]} animationDuration={800}>
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

export default UserGrowthChart;
