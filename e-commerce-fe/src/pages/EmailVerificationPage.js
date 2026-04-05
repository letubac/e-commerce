/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Mail, CheckCircle, AlertCircle, Loader, ArrowLeft } from 'lucide-react';

const EmailVerificationPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading'); // loading, success, error, resend
  const [message, setMessage] = useState('');
  const [email, setEmail] = useState('');

  const token = searchParams.get('token');
  const emailParam = searchParams.get('email');

  const verifyEmail = useCallback(async (verificationToken) => {
    try {
      // Mock API call - thay thế bằng API thực tế
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // Giả lập response
      const success = Math.random() > 0.3; // 70% thành công
      
      if (success) {
        setStatus('success');
        setMessage('Email của bạn đã được xác thực thành công!');
        
        // Redirect về login sau 3 giây
        setTimeout(() => {
          navigate('/login', { 
            state: { message: 'Email đã được xác thực. Vui lòng đăng nhập.' }
          });
        }, 3000);
      } else {
        setStatus('error');
        setMessage('Liên kết xác thực đã hết hạn hoặc không hợp lệ');
      }
    } catch (error) {
      setStatus('error');
      setMessage('Có lỗi xảy ra khi xác thực email. Vui lòng thử lại.');
    }
  }, [navigate]);

  useEffect(() => {
    if (token) {
      // Verify email với token
      verifyEmail(token);
    } else if (emailParam) {
      setEmail(emailParam);
      setStatus('resend');
    } else {
      setStatus('error');
      setMessage('Liên kết xác thực không hợp lệ');
    }
  }, [token, emailParam, verifyEmail]);

  const resendVerification = async () => {
    if (!email) return;
    
    setStatus('loading');
    try {
      // Mock API call
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      setStatus('resend');
      setMessage('Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư của bạn.');
    } catch (error) {
      setStatus('error');
      setMessage('Không thể gửi lại email xác thực. Vui lòng thử lại sau.');
    }
  };

  const renderContent = () => {
    switch (status) {
      case 'loading':
        return (
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <Loader className="h-16 w-16 text-red-600 animate-spin" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              Đang xác thực email...
            </h1>
            <p className="text-gray-600">
              Vui lòng chờ trong giây lát
            </p>
          </div>
        );

      case 'success':
        return (
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <CheckCircle className="h-16 w-16 text-green-600" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              Xác thực thành công!
            </h1>
            <p className="text-gray-600 mb-6">
              {message}
            </p>
            <p className="text-sm text-gray-500">
              Bạn sẽ được chuyển hướng đến trang đăng nhập trong vài giây...
            </p>
          </div>
        );

      case 'error':
        return (
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <AlertCircle className="h-16 w-16 text-red-600" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              Xác thực thất bại
            </h1>
            <p className="text-gray-600 mb-6">
              {message}
            </p>
            <div className="space-y-3">
              <button
                onClick={() => navigate('/register')}
                className="w-full bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition-colors"
              >
                Đăng ký lại
              </button>
              <button
                onClick={() => navigate('/login')}
                className="w-full border border-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-50 transition-colors"
              >
                Về trang đăng nhập
              </button>
            </div>
          </div>
        );

      case 'resend':
        return (
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <Mail className="h-16 w-16 text-blue-600" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              Xác thực email
            </h1>
            <p className="text-gray-600 mb-6">
              {message || 'Chúng tôi đã gửi email xác thực đến địa chỉ email của bạn. Vui lòng kiểm tra hộp thư và nhấn vào liên kết xác thực.'}
            </p>
            
            {email && (
              <div className="bg-gray-50 rounded-lg p-4 mb-6">
                <p className="text-sm text-gray-600">
                  Email đã gửi đến: <span className="font-medium">{email}</span>
                </p>
              </div>
            )}

            <div className="space-y-3">
              <button
                onClick={resendVerification}
                className="w-full bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition-colors"
              >
                Gửi lại email xác thực
              </button>
              
              <div className="text-sm text-gray-500">
                <p>Không nhận được email?</p>
                <ul className="mt-2 space-y-1 text-left">
                  <li>• Kiểm tra thư mục spam/junk</li>
                  <li>• Đảm bảo địa chỉ email chính xác</li>
                  <li>• Chờ thêm vài phút</li>
                </ul>
              </div>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="bg-white rounded-lg shadow-lg p-8">
          {renderContent()}
          
          <div className="mt-8 pt-6 border-t border-gray-200">
            <button
              onClick={() => navigate('/')}
              className="w-full flex items-center justify-center text-gray-600 hover:text-gray-900 transition-colors"
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Về trang chủ
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmailVerificationPage;