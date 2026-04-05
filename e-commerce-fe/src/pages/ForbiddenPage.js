/**
 * author: LeTuBac
 */
import React from 'react';
import { Link } from 'react-router-dom';
import { Shield, ArrowLeft } from 'lucide-react';

const ForbiddenPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
        <div className="flex justify-center mb-6">
          <Shield className="h-16 w-16 text-red-600" />
        </div>
        
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          403 - Truy cập bị từ chối
        </h1>
        
        <p className="text-gray-600 mb-6">
          Bạn không có quyền truy cập trang này. Vui lòng đăng nhập với tài khoản có quyền phù hợp.
        </p>
        
        <div className="space-y-3">
          <Link
            to="/"
            className="w-full inline-flex items-center justify-center px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Về trang chủ
          </Link>
          
          <Link
            to="/login"
            className="w-full inline-flex items-center justify-center px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
          >
            Đăng nhập
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForbiddenPage;