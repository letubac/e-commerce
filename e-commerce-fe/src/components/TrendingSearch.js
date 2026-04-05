/**
 * author: LeTuBac
 */
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Smartphone, 
  Laptop, 
  MonitorSpeaker, 
  Tablet, 
  Watch, 
  Monitor,
  Headphones,
  HardDrive
} from 'lucide-react';

export default function TrendingSearch() {
  const navigate = useNavigate();

  const trendingCategories = [
    {
      id: 1,
      name: 'Điện thoại',
      icon: <Smartphone className="w-8 h-8" />,
      bgColor: 'bg-blue-500',
      route: '/products?category=smartphone'
    },
    {
      id: 2,
      name: 'Laptop',
      icon: <Laptop className="w-8 h-8" />,
      bgColor: 'bg-green-500',
      route: '/products?category=laptop'
    },
    {
      id: 3,
      name: 'Máy tính để bàn',
      icon: <MonitorSpeaker className="w-8 h-8" />,
      bgColor: 'bg-purple-500',
      route: '/products?category=desktop'
    },
    {
      id: 4,
      name: 'Tablet',
      icon: <Tablet className="w-8 h-8" />,
      bgColor: 'bg-orange-500',
      route: '/products?category=tablet'
    },
    {
      id: 5,
      name: 'Đồng hồ thông minh',
      icon: <Watch className="w-8 h-8" />,
      bgColor: 'bg-red-500',
      route: '/products?category=smartwatch'
    },
    {
      id: 6,
      name: 'Màn hình',
      icon: <Monitor className="w-8 h-8" />,
      bgColor: 'bg-indigo-500',
      route: '/products?category=monitor'
    },
    {
      id: 7,
      name: 'Phụ kiện',
      icon: <Headphones className="w-8 h-8" />,
      bgColor: 'bg-pink-500',
      route: '/products?category=accessories'
    },
    {
      id: 8,
      name: 'Thiết bị thông minh',
      icon: <HardDrive className="w-8 h-8" />,
      bgColor: 'bg-teal-500',
      route: '/products?category=smart-devices'
    }
  ];

  const handleCategoryClick = (route) => {
    navigate(route);
  };

  return (
    <div className="bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Xu hướng tìm kiếm</h2>
          <button 
            onClick={() => navigate('/products')}
            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors font-medium"
          >
            XEM THÊM
          </button>
        </div>

        <div className="grid grid-cols-8 gap-4">
          {trendingCategories.map((category) => (
            <div
              key={category.id}
              onClick={() => handleCategoryClick(category.route)}
              className="group cursor-pointer"
            >
              <div className="bg-white rounded-lg p-4 text-center hover:shadow-lg transition-all duration-300 group-hover:-translate-y-1">
                <div className={`${category.bgColor} w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-3 text-white group-hover:scale-110 transition-transform duration-300`}>
                  {category.icon}
                </div>
                <h3 className="text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                  {category?.name || 'Chưa có tên'}
                </h3>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}