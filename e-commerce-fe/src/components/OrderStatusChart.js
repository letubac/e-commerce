/**
 * author: LeTuBac
 */
import React from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Legend,
  Tooltip,
  ResponsiveContainer
} from 'recharts';

/**
 * Order Status Distribution Chart
 * Shows breakdown of orders by status
 */
function OrderStatusChart({ data }) {
  // Ensure data is object
  const validData = (typeof data === 'object' && data !== null) ? data : {};
  
  const getChartData = () => {
    const chartData = [];
    const statusConfig = {
      'PENDING': { label: 'Chờ xử lý', color: '#ff9800' },
      'PROCESSING': { label: 'Đang xử lý', color: '#2196f3' },
      'SHIPPED': { label: 'Đã vận chuyển', color: '#00bcd4' },
      'DELIVERED': { label: 'Đã giao', color: '#4caf50' },
      'CANCELLED': { label: 'Bị hủy', color: '#f44336' }
    };

    Object.entries(validData).forEach(([key, value]) => {
      const status = key.toUpperCase().replace(/Orders$/, '').toUpperCase();
      if (statusConfig[status] && value > 0) {
        chartData.push({
          name: statusConfig[status].label,
          value: value,
          color: statusConfig[status].color
        });
      }
    });

    return chartData.length > 0
      ? chartData
      : [{ name: 'Không có dữ liệu', value: 1, color: '#e0e0e0' }];
  };

  const chartData = getChartData();

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white p-2 border border-gray-300 rounded shadow-lg">
          <p style={{ color: payload[0].fill }} className="text-sm font-medium">
            {payload[0].payload.name}
          </p>
          <p className="text-sm text-gray-600">{payload[0].value} đơn hàng</p>
          <p className="text-xs text-gray-500">
            {((payload[0].value / chartData.reduce((sum, item) => sum + item.value, 0)) * 100).toFixed(1)}%
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full h-80">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={({ name, value }) => `${name}: ${value}`}
            outerRadius={80}
            fill="#8884d8"
            dataKey="value"
            innerRadius={40}
            animationBegin={0}
            animationDuration={800}
          >
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
          <Legend
            layout="vertical"
            align="right"
            verticalAlign="middle"
            contentStyle={{ fontSize: '12px', color: '#6b7280' }}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}

export default OrderStatusChart;
