import React from 'react';
import { MapPin, Phone, Facebook, Twitter, Instagram, Youtube } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-gray-100 pt-12 pb-6">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-5 gap-8">
          {/* Company Info */}
          <div className="col-span-1">
            <h3 className="font-bold text-gray-900 mb-4">CÔNG TY TNHH E-SHOP COMPUTER</h3>
            <p className="text-gray-600 text-sm mb-6 leading-relaxed">
              Với các giải pháp công nghệ tốt nhất, E-SHOPVN là tất cả những gì bạn cần để có một góc làm việc tối ưu nhất.
            </p>
            
            {/* Certification Badge */}
            <div className="mb-6">
              <div className="bg-blue-600 text-white px-3 py-2 rounded-lg inline-flex items-center gap-2 text-sm">
                <div className="w-6 h-6 bg-white rounded-full flex items-center justify-center">
                  <span className="text-blue-600 font-bold text-xs">✓</span>
                </div>
                <div>
                  <div className="font-bold">ĐÃ THÔNG BÁO</div>
                  <div className="text-xs">BỘ CÔNG THƯƠNG</div>
                </div>
              </div>
            </div>

            {/* Social Links */}
            <div className="flex gap-3">
              <a href="#" className="w-8 h-8 bg-blue-600 text-white rounded flex items-center justify-center hover:bg-blue-700 transition-colors">
                <Facebook className="w-4 h-4" />
              </a>
              <a href="#" className="w-8 h-8 bg-blue-400 text-white rounded flex items-center justify-center hover:bg-blue-500 transition-colors">
                <Twitter className="w-4 h-4" />
              </a>
              <a href="#" className="w-8 h-8 bg-pink-500 text-white rounded flex items-center justify-center hover:bg-pink-600 transition-colors">
                <Instagram className="w-4 h-4" />
              </a>
              <a href="#" className="w-8 h-8 bg-red-600 text-white rounded flex items-center justify-center hover:bg-red-700 transition-colors">
                <Youtube className="w-4 h-4" />
              </a>
            </div>
          </div>

          {/* Contact Info */}
          <div className="col-span-1">
            <h3 className="font-bold text-gray-900 mb-4">Thông tin liên hệ</h3>
            <div className="space-y-3">
              <div className="flex items-start gap-2">
                <MapPin className="w-4 h-4 text-gray-500 mt-0.5 flex-shrink-0" />
                <span className="text-sm text-gray-600">
                  Tự Lập, Phường 4, Tân Bình, Hồ Chí Minh 700000, Việt Nam
                </span>
              </div>
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-gray-500" />
                <span className="text-sm text-gray-600">0123456789</span>
              </div>
            </div>
          </div>

          {/* Customer Support */}
          <div className="col-span-1">
            <h3 className="font-bold text-gray-900 mb-4">Hỗ trợ khách hàng</h3>
            <ul className="space-y-2">
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  FAQ
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Về chúng tôi
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Phương thức thanh toán
                </a>
              </li>
            </ul>
          </div>

          {/* Connections */}
          <div className="col-span-1">
            <h3 className="font-bold text-gray-900 mb-4">Liên kết</h3>
            <ul className="space-y-2">
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Tất cả sản phẩm
                </a>
              </li>
            </ul>
          </div>

          {/* Policies */}
          <div className="col-span-1">
            <h3 className="font-bold text-gray-900 mb-4">Chính sách</h3>
            <ul className="space-y-2">
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Chính sách Đổi Trả
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Chính sách Bảo Mật Thông Tin
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Chính sách Vận Chuyển
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Điều Khoản và Điều Kiện
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-gray-600 hover:text-red-600 transition-colors">
                  Chính sách Khiếu Nại
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Copyright */}
        <div className="border-t border-gray-200 mt-8 pt-6 text-center">
          <p className="text-sm text-gray-600">
            Copyright © 2025 E-SHOPVN. Powered by E-SHOP
          </p>
        </div>
      </div>
    </footer>
  );
}