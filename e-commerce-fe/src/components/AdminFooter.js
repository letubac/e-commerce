import React from 'react';
import { Heart, ExternalLink } from 'lucide-react';

export default function AdminFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {/* Company info */}
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center space-x-2 mb-3">
              <div className="w-6 h-6 bg-red-600 rounded flex items-center justify-center">
                <span className="text-white font-bold text-xs">E</span>
              </div>
              <span className="font-bold text-gray-900">E-SHOP Admin</span>
            </div>
            <p className="text-sm text-gray-600 mb-3">
              Hệ thống quản trị E-commerce toàn diện cho việc quản lý cửa hàng trực tuyến.
            </p>
            <div className="flex items-center space-x-4 text-sm text-gray-500">
              <span>Version 1.0.0</span>
              <span>•</span>
              <span>Build 2024.11.01</span>
            </div>
          </div>

          {/* Quick links */}
          <div>
            <h3 className="font-medium text-gray-900 mb-3">Liên kết nhanh</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <a href="#" className="text-gray-600 hover:text-red-600 flex items-center space-x-1">
                  <span>Hướng dẫn sử dụng</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a href="#" className="text-gray-600 hover:text-red-600 flex items-center space-x-1">
                  <span>API Documentation</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a href="#" className="text-gray-600 hover:text-red-600 flex items-center space-x-1">
                  <span>Báo cáo lỗi</span>
                  <ExternalLink size={12} />
                </a>
              </li>
              <li>
                <a href="#" className="text-gray-600 hover:text-red-600 flex items-center space-x-1">
                  <span>Liên hệ hỗ trợ</span>
                  <ExternalLink size={12} />
                </a>
              </li>
            </ul>
          </div>

          {/* System status */}
          <div>
            <h3 className="font-medium text-gray-900 mb-3">Trạng thái hệ thống</h3>
            <div className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-600">Database</span>
                <span className="flex items-center space-x-1">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span className="text-green-600">Online</span>
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600">API Server</span>
                <span className="flex items-center space-x-1">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span className="text-green-600">Online</span>
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600">Payment Gateway</span>
                <span className="flex items-center space-x-1">
                  <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
                  <span className="text-yellow-600">Warning</span>
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600">Email Service</span>
                <span className="flex items-center space-x-1">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span className="text-green-600">Online</span>
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom section */}
        <div className="border-t border-gray-200 mt-6 pt-6 flex flex-col md:flex-row items-center justify-between">
          <div className="flex items-center space-x-1 text-sm text-gray-600">
            <span>&copy; {currentYear} E-SHOP. Made with</span>
            <Heart size={14} className="text-red-500" fill="currentColor" />
            <span>by Development Team</span>
          </div>
          
          <div className="flex items-center space-x-6 mt-4 md:mt-0 text-sm text-gray-500">
            <a href="#" className="hover:text-gray-700">Privacy Policy</a>
            <a href="#" className="hover:text-gray-700">Terms of Service</a>
            <a href="#" className="hover:text-gray-700">Security</a>
            <span>Last updated: {new Date().toLocaleDateString('vi-VN')}</span>
          </div>
        </div>
      </div>
    </footer>
  );
}