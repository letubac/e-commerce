import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  User, 
  Edit3, 
  CreditCard, 
  ShoppingBag, 
  RotateCcw, 
  Heart, 
  Lock, 
  Camera,
  Save,
  ArrowLeft
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api, { getImageUrl } from '../api/api';
import { getFavorites, removeFavorite } from '../utils/favoritesUtils';

function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState([]);
  const [favorites, setFavorites] = useState([]);

  // Profile form state
  const [profileData, setProfileData] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: user?.address || '',
    city: user?.city || '',
    district: user?.district || '',
    dateOfBirth: user?.dateOfBirth || '',
    gender: user?.gender || ''
  });

  // Password change state
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [editMode, setEditMode] = useState(false);

  useEffect(() => {
    if (activeTab === 'orders') {
      fetchOrders();
    } else if (activeTab === 'favorites') {
      fetchFavorites();
    }
  }, [activeTab]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await api.getOrders();
      setOrders(data.content || data || []);
    } catch (error) {
      console.error('Error fetching orders:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchFavorites = async () => {
    try {
      setLoading(true);
      // Load from localStorage (toggled from product pages)
      setFavorites(getFavorites());
    } catch (error) {
      console.error('Error fetching favorites:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleProfileUpdate = async () => {
    try {
      setLoading(true);
      // await api.updateProfile(profileData);
      console.log('Updating profile:', profileData);
      alert('Cập nhật thông tin thành công!');
      setEditMode(false);
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Có lỗi xảy ra khi cập nhật thông tin');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert('Mật khẩu xác nhận không khớp');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      alert('Mật khẩu mới phải có ít nhất 6 ký tự');
      return;
    }

    try {
      setLoading(true);
      // await api.changePassword(passwordData);
      console.log('Changing password');
      alert('Đổi mật khẩu thành công!');
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      console.error('Error changing password:', error);
      alert('Có lỗi xảy ra khi đổi mật khẩu');
    } finally {
      setLoading(false);
    }
  };

  const tabs = [
    { id: 'profile', label: 'Hồ sơ cá nhân', icon: User },
    { id: 'orders', label: 'Lịch sử đơn hàng', icon: ShoppingBag },
    { id: 'returns', label: 'Lịch sử trả hàng', icon: RotateCcw },
    { id: 'favorites', label: 'Sản phẩm yêu thích', icon: Heart },
    { id: 'payment', label: 'Thanh toán', icon: CreditCard },
    { id: 'password', label: 'Đổi mật khẩu', icon: Lock }
  ];

  const getInitials = (name) => {
    return name?.split(' ').map(n => n[0]).join('').toUpperCase() || 'U';
  };

  const renderProfileTab = () => (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Thông tin cá nhân</h2>
        <button
          onClick={() => setEditMode(!editMode)}
          className="flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg transition"
        >
          <Edit3 size={16} />
          {editMode ? 'Hủy' : 'Chỉnh sửa'}
        </button>
      </div>

      {/* Avatar Section */}
      <div className="flex items-center mb-8">
        <div className="relative">
          <div className="w-24 h-24 bg-red-600 rounded-full flex items-center justify-center text-white text-2xl font-bold">
            {getInitials(profileData.fullName)}
          </div>
          {editMode && (
            <button className="absolute -bottom-2 -right-2 w-8 h-8 bg-gray-600 text-white rounded-full flex items-center justify-center hover:bg-gray-700">
              <Camera size={16} />
            </button>
          )}
        </div>
        <div className="ml-6">
          <h3 className="text-xl font-semibold text-gray-800">{profileData.fullName}</h3>
          <p className="text-gray-600">{profileData.email}</p>
          <p className="text-sm text-green-600 mt-1">Tài khoản đã xác thực</p>
        </div>
      </div>

      {/* Profile Form */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Họ và tên</label>
          <input
            type="text"
            value={profileData.fullName}
            onChange={(e) => setProfileData(prev => ({ ...prev, fullName: e.target.value }))}
            disabled={!editMode}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <input
            type="email"
            value={profileData.email}
            onChange={(e) => setProfileData(prev => ({ ...prev, email: e.target.value }))}
            disabled={!editMode}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
          <input
            type="tel"
            value={profileData.phone}
            onChange={(e) => setProfileData(prev => ({ ...prev, phone: e.target.value }))}
            disabled={!editMode}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Ngày sinh</label>
          <input
            type="date"
            value={profileData.dateOfBirth}
            onChange={(e) => setProfileData(prev => ({ ...prev, dateOfBirth: e.target.value }))}
            disabled={!editMode}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Giới tính</label>
          <select
            value={profileData.gender}
            onChange={(e) => setProfileData(prev => ({ ...prev, gender: e.target.value }))}
            disabled={!editMode}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          >
            <option value="">Chọn giới tính</option>
            <option value="male">Nam</option>
            <option value="female">Nữ</option>
            <option value="other">Khác</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Thành phố</label>
          <input
            type="text"
            value={profileData.city}
            onChange={(e) => setProfileData(prev => ({ ...prev, city: e.target.value }))}
            disabled={!editMode}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          />
        </div>

        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">Địa chỉ</label>
          <textarea
            value={profileData.address}
            onChange={(e) => setProfileData(prev => ({ ...prev, address: e.target.value }))}
            disabled={!editMode}
            rows="3"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-50"
          />
        </div>
      </div>

      {editMode && (
        <div className="flex justify-end mt-6">
          <button
            onClick={handleProfileUpdate}
            disabled={loading}
            className="flex items-center gap-2 px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition disabled:opacity-50"
          >
            <Save size={16} />
            {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>
      )}
    </div>
  );

  const renderOrdersTab = () => (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Lịch sử đơn hàng</h2>
      
      {loading ? (
        <div className="text-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-red-600 mx-auto"></div>
          <p className="text-gray-600 mt-2">Đang tải...</p>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-12">
          <ShoppingBag size={48} className="mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-semibold text-gray-600 mb-2">Chưa có đơn hàng nào</h3>
          <p className="text-gray-500 mb-4">Bạn chưa có đơn hàng nào. Hãy bắt đầu mua sắm!</p>
          <button
            onClick={() => navigate('/products')}
            className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            Mua sắm ngay
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.id} className="border border-gray-200 rounded-lg p-4">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <h4 className="font-semibold">Đơn hàng #{order.id}</h4>
                  <p className="text-sm text-gray-600">
                    {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                  </p>
                </div>
                <span className={`px-3 py-1 rounded-full text-sm ${
                  order.status === 'completed' ? 'bg-green-100 text-green-800' :
                  order.status === 'processing' ? 'bg-yellow-100 text-yellow-800' :
                  order.status === 'cancelled' ? 'bg-red-100 text-red-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {order.status === 'completed' ? 'Hoàn thành' :
                   order.status === 'processing' ? 'Đang xử lý' :
                   order.status === 'cancelled' ? 'Đã hủy' : 'Chờ xác nhận'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <p className="text-lg font-semibold text-red-600">
                  {order.totalPrice?.toLocaleString('vi-VN')}₫
                </p>
                <button className="text-red-600 hover:text-red-700 text-sm font-medium">
                  Xem chi tiết
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  const renderFavoritesTab = () => (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Sản phẩm yêu thích</h2>
      
      {loading ? (
        <div className="text-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-red-600 mx-auto" />
          <p className="text-gray-600 mt-2">Đang tải...</p>
        </div>
      ) : favorites.length === 0 ? (
        <div className="text-center py-12">
          <Heart size={48} className="mx-auto text-gray-400 mb-4" />
          <h3 className="text-lg font-semibold text-gray-600 mb-2">Chưa có sản phẩm yêu thích</h3>
          <p className="text-gray-500 mb-4">Nhấn biểu tượng ❤️ trên trang sản phẩm để thêm vào danh sách yêu thích!</p>
          <button
            onClick={() => navigate('/products')}
            className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            Khám phá sản phẩm
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {favorites.map((product) => {
            const imgSrc = getImageUrl(product.imageUrl);
            return (
              <div key={product.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
                <div
                  className="cursor-pointer"
                  onClick={() => navigate(`/product/${product.id}`)}
                >
                  {imgSrc ? (
                    <img
                      src={imgSrc}
                      alt={product.name}
                      className="w-full h-48 object-cover rounded-lg mb-4"
                      onError={(e) => { e.target.style.display = 'none'; }}
                    />
                  ) : (
                    <div className="w-full h-48 bg-gray-100 rounded-lg mb-4 flex items-center justify-center">
                      <Heart size={36} className="text-gray-300" />
                    </div>
                  )}
                  <h4 className="font-semibold mb-2 line-clamp-2 text-gray-800">{product.name}</h4>
                  <p className="text-red-600 font-bold text-lg mb-3">
                    {(product.price || 0).toLocaleString('vi-VN')}₫
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => navigate(`/product/${product.id}`)}
                    className="flex-1 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition text-sm"
                  >
                    Xem sản phẩm
                  </button>
                  <button
                    onClick={() => { removeFavorite(product.id); setFavorites(getFavorites()); }}
                    className="p-2 border border-gray-300 rounded-lg hover:bg-red-50 hover:border-red-300 transition"
                    title="Xóa khỏi yêu thích"
                  >
                    <Heart size={16} className="text-red-600" fill="currentColor" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  const renderPasswordTab = () => (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Đổi mật khẩu</h2>
      
      <div className="max-w-md space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Mật khẩu hiện tại</label>
          <input
            type="password"
            value={passwordData.currentPassword}
            onChange={(e) => setPasswordData(prev => ({ ...prev, currentPassword: e.target.value }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Mật khẩu mới</label>
          <input
            type="password"
            value={passwordData.newPassword}
            onChange={(e) => setPasswordData(prev => ({ ...prev, newPassword: e.target.value }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Xác nhận mật khẩu mới</label>
          <input
            type="password"
            value={passwordData.confirmPassword}
            onChange={(e) => setPasswordData(prev => ({ ...prev, confirmPassword: e.target.value }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
          />
        </div>

        <button
          onClick={handlePasswordChange}
          disabled={loading || !passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword}
          className="w-full py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Đang cập nhật...' : 'Đổi mật khẩu'}
        </button>
      </div>
    </div>
  );

  const renderPaymentTab = () => (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Quản lý thanh toán</h2>
      
      <div className="text-center py-12">
        <CreditCard size={48} className="mx-auto text-gray-400 mb-4" />
        <h3 className="text-lg font-semibold text-gray-600 mb-2">Chưa có thông tin thanh toán</h3>
        <p className="text-gray-500 mb-4">Thêm thẻ tín dụng hoặc phương thức thanh toán để checkout nhanh hơn</p>
        <button className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition">
          Thêm phương thức thanh toán
        </button>
      </div>
    </div>
  );

  const renderReturnsTab = () => (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Lịch sử trả hàng</h2>
      
      <div className="text-center py-12">
        <RotateCcw size={48} className="mx-auto text-gray-400 mb-4" />
        <h3 className="text-lg font-semibold text-gray-600 mb-2">Chưa có yêu cầu trả hàng</h3>
        <p className="text-gray-500">Bạn chưa có yêu cầu trả hàng nào</p>
      </div>
    </div>
  );

  const renderContent = () => {
    switch (activeTab) {
      case 'profile': return renderProfileTab();
      case 'orders': return renderOrdersTab();
      case 'favorites': return renderFavoritesTab();
      case 'password': return renderPasswordTab();
      case 'payment': return renderPaymentTab();
      case 'returns': return renderReturnsTab();
      default: return renderProfileTab();
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex items-center mb-6">
          <button
            onClick={() => navigate(-1)}
            className="mr-4 p-2 hover:bg-gray-200 rounded-lg transition"
          >
            <ArrowLeft size={24} />
          </button>
          <h1 className="text-2xl font-bold text-gray-800">Tài khoản của tôi</h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6">
              {/* User Info */}
              <div className="flex items-center mb-6 pb-6 border-b border-gray-200">
                <div className="w-12 h-12 bg-red-600 rounded-full flex items-center justify-center text-white font-bold">
                  {getInitials(user?.fullName)}
                </div>
                <div className="ml-3">
                  <h3 className="font-semibold text-gray-800">{user?.fullName}</h3>
                  <p className="text-sm text-gray-600">{user?.email}</p>
                </div>
              </div>

              {/* Navigation */}
              <nav className="space-y-1">
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`w-full flex items-center gap-3 px-3 py-2 text-left rounded-lg transition ${
                        activeTab === tab.id
                          ? 'bg-red-50 text-red-600 border-l-4 border-red-600'
                          : 'text-gray-700 hover:bg-gray-50'
                      }`}
                    >
                      <Icon size={18} />
                      <span className="text-sm font-medium">{tab.label}</span>
                    </button>
                  );
                })}
                
                <button
                  onClick={logout}
                  className="w-full flex items-center gap-3 px-3 py-2 text-left rounded-lg transition text-red-600 hover:bg-red-50 mt-4 pt-4 border-t border-gray-200"
                >
                  <ArrowLeft size={18} />
                  <span className="text-sm font-medium">Đăng xuất</span>
                </button>
              </nav>
            </div>
          </div>

          {/* Main Content */}
          <div className="lg:col-span-3">
            {renderContent()}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProfilePage;