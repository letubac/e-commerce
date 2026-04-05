/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Users, Search, Lock, Unlock, Eye, Mail, Phone, Calendar } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [filters, setFilters] = useState({
    search: '',
    role: '',
    status: '',
    page: 0,
    size: 10
  });
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0
  });

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = {
        page: filters.page,
        size: filters.size,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      };

      if (filters.search) params.search = filters.search;
      if (filters.role) params.role = filters.role;
      if (filters.status) params.status = filters.status;

      const response = await adminApi.getUsers(params);
      
      // parseBusinessResponse đã trả về data
      setUsers(response.content || []);
      setPagination({
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      });
    } catch (error) {
      console.error('Error fetching users:', error);
      toast.error('Không thể tải danh sách người dùng');
    } finally {
      setLoading(false);
    }
  };

  const handleViewUser = async (userId) => {
    try {
      const userDetails = await adminApi.getUserById(userId);
      setSelectedUser(userDetails);
      setShowModal(true);
    } catch (error) {
      console.error('Error fetching user details:', error);
      toast.error('Không thể tải thông tin người dùng');
    }
  };

  const handleLockUser = async (userId) => {
    if (!window.confirm('Bạn có chắc muốn khóa người dùng này?')) return;
    
    try {
      await adminApi.lockUser(userId);
      toast.success('Khóa người dùng thành công');
      fetchUsers();
    } catch (error) {
      console.error('Error locking user:', error);
      toast.error('Không thể khóa người dùng');
    }
  };

  const handleUnlockUser = async (userId) => {
    try {
      await adminApi.unlockUser(userId);
      toast.success('Mở khóa người dùng thành công');
      fetchUsers();
    } catch (error) {
      console.error('Error unlocking user:', error);
      toast.error('Không thể mở khóa người dùng');
    }
  };

  const getRoleBadge = (role) => {
    const roleConfig = {
      ROLE_SUPER_ADMIN: { bg: 'bg-purple-100', text: 'text-purple-800', label: 'Super Admin' },
      ROLE_ADMIN: { bg: 'bg-red-100', text: 'text-red-800', label: 'Admin' },
      ROLE_CUSTOMER: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'Khách hàng' },
      ROLE_USER: { bg: 'bg-green-100', text: 'text-green-800', label: 'Người dùng' }
    };

    const config = roleConfig[role] || { bg: 'bg-gray-100', text: 'text-gray-800', label: role };
    return (
      <span className={`px-2 py-1 text-xs rounded-full ${config.bg} ${config.text}`}>
        {config.label}
      </span>
    );
  };

  const getStatusBadge = (active, emailVerified) => {
    if (!active) {
      return (
        <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800 flex items-center gap-1">
          <Lock size={12} />
          Đã khóa
        </span>
      );
    }
    if (emailVerified) {
      return (
        <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800 flex items-center gap-1">
          <Unlock size={12} />
          Hoạt động
        </span>
      );
    }
    return (
      <span className="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-800">
        Chưa xác thực email
      </span>
    );
  };

  if (loading && users.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div className="animate-pulse space-y-4">
          <div className="h-10 bg-gray-200 rounded w-1/4"></div>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <h3 className="text-lg font-semibold text-gray-900">Quản lý người dùng</h3>
          
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                placeholder="Tìm kiếm người dùng..."
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value, page: 0 })}
              />
            </div>
            
            <select 
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
              value={filters.role}
              onChange={(e) => setFilters({ ...filters, role: e.target.value, page: 0 })}
            >
              <option value="">Tất cả vai trò</option>
              <option value="ROLE_CUSTOMER">Khách hàng</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Người dùng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Vai trò
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ngày tạo
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users.length > 0 ? users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                      <div className="h-10 w-10 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center text-white font-semibold">
                        {user.firstName?.[0]}{user.lastName?.[0]}
                      </div>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">
                        {user.firstName} {user.lastName}
                      </div>
                      <div className="text-sm text-gray-500">
                        ID: {user.id}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center text-sm text-gray-900">
                    <Mail size={14} className="mr-1 text-gray-400" />
                    {user.email}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getRoleBadge(user.role)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getStatusBadge(user.active, user.emailVerified)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(user.createdAt).toLocaleDateString('vi-VN')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex gap-2">
                    <button 
                      onClick={() => handleViewUser(user.id)}
                      className="text-blue-600 hover:text-blue-900 flex items-center gap-1"
                    >
                      <Eye size={16} />
                      Xem
                    </button>
                    {user.active ? (
                      <button 
                        onClick={() => handleLockUser(user.id)}
                        className="text-red-600 hover:text-red-900 flex items-center gap-1"
                      >
                        <Lock size={16} />
                        Khóa
                      </button>
                    ) : (
                      <button 
                        onClick={() => handleUnlockUser(user.id)}
                        className="text-green-600 hover:text-green-900 flex items-center gap-1"
                      >
                        <Unlock size={16} />
                        Mở khóa
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            )) : (
              <tr>
                <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                  <Users className="mx-auto mb-2 text-gray-300" size={48} />
                  <p>Không có người dùng nào</p>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
          <div className="text-sm text-gray-500">
            Hiển thị {users.length} / {pagination.totalElements} người dùng
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setFilters({ ...filters, page: filters.page - 1 })}
              disabled={filters.page === 0}
              className="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Trước
            </button>
            <span className="px-3 py-1">
              Trang {filters.page + 1} / {pagination.totalPages}
            </span>
            <button
              onClick={() => setFilters({ ...filters, page: filters.page + 1 })}
              disabled={filters.page >= pagination.totalPages - 1}
              className="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Sau
            </button>
          </div>
        </div>
      )}

      {/* User Detail Modal */}
      {showModal && selectedUser && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">
                Thông tin người dùng
              </h3>
              <button
                onClick={() => {
                  setShowModal(false);
                  setSelectedUser(null);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>

            <div className="p-6 space-y-6">
              <div className="flex items-center space-x-4">
                <div className="h-20 w-20 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center text-white text-2xl font-semibold">
                  {selectedUser.firstName?.[0]}{selectedUser.lastName?.[0]}
                </div>
                <div>
                  <h4 className="text-xl font-semibold text-gray-900">
                    {selectedUser.firstName} {selectedUser.lastName}
                  </h4>
                  <div className="flex items-center gap-2 mt-1">
                    {getRoleBadge(selectedUser.role)}
                    {getStatusBadge(selectedUser.accountLocked, selectedUser.enabled)}
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <div className="text-sm font-medium text-gray-500 mb-1">Email</div>
                  <div className="flex items-center text-gray-900">
                    <Mail size={16} className="mr-2 text-gray-400" />
                    {selectedUser.email}
                  </div>
                </div>

                <div>
                  <div className="text-sm font-medium text-gray-500 mb-1">Số điện thoại</div>
                  <div className="flex items-center text-gray-900">
                    <Phone size={16} className="mr-2 text-gray-400" />
                    {selectedUser.phoneNumber || 'Chưa cập nhật'}
                  </div>
                </div>

                <div>
                  <div className="text-sm font-medium text-gray-500 mb-1">Ngày tạo</div>
                  <div className="flex items-center text-gray-900">
                    <Calendar size={16} className="mr-2 text-gray-400" />
                    {new Date(selectedUser.createdAt).toLocaleString('vi-VN')}
                  </div>
                </div>

                <div>
                  <div className="text-sm font-medium text-gray-500 mb-1">Cập nhật lần cuối</div>
                  <div className="flex items-center text-gray-900">
                    <Calendar size={16} className="mr-2 text-gray-400" />
                    {new Date(selectedUser.updatedAt).toLocaleString('vi-VN')}
                  </div>
                </div>
              </div>

              <div className="border-t border-gray-200 pt-4">
                <h5 className="font-semibold text-gray-900 mb-3">Thông tin bổ sung</h5>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Email đã xác thực:</span>
                    <span className={selectedUser.emailVerified ? 'text-green-600' : 'text-red-600'}>
                      {selectedUser.emailVerified ? 'Có' : 'Chưa'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">2FA:</span>
                    <span className={selectedUser.twoFactorEnabled ? 'text-green-600' : 'text-gray-600'}>
                      {selectedUser.twoFactorEnabled ? 'Đã bật' : 'Chưa bật'}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default UserManagement;
