/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import {
  CheckSquare, Plus, Edit, Trash2, Filter, Grid3X3, List,
  Clock, AlertCircle, XCircle
} from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

const STATUSES = ['ALL', 'TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'];
const PRIORITIES = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'URGENT'];
const ROLES = ['ADMIN', 'CUSTOMER', 'SUPPORT', 'SUPER_ADMIN'];

const PRIORITY_COLORS = {
  LOW: 'bg-blue-100 text-blue-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  HIGH: 'bg-orange-100 text-orange-800',
  URGENT: 'bg-red-100 text-red-800',
};

const STATUS_COLORS = {
  TODO: 'bg-gray-100 text-gray-800',
  IN_PROGRESS: 'bg-blue-100 text-blue-800',
  DONE: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

const KANBAN_COLUMNS = ['TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'];

const DEFAULT_FORM = {
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM',
  assignedRole: 'ADMIN',
  dueDate: '',
};

function TaskManagement() {
  const [tasks, setTasks] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [viewMode, setViewMode] = useState('list'); // 'list' | 'kanban'
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const fetchTasks = useCallback(async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterStatus !== 'ALL') params.status = filterStatus;
      if (filterPriority !== 'ALL') params.priority = filterPriority;
      const data = await adminApi.getTasks(params);
      setTasks(Array.isArray(data) ? data : []);
    } catch (err) {
      toast.error(err.message || 'Không thể tải danh sách task');
    } finally {
      setLoading(false);
    }
  }, [filterStatus, filterPriority]);

  const fetchStatistics = useCallback(async () => {
    try {
      const data = await adminApi.getTaskStatistics();
      setStatistics(data);
    } catch {
      // statistics are optional
    }
  }, []);

  useEffect(() => {
    fetchTasks();
    fetchStatistics();
  }, [fetchTasks, fetchStatistics]);

  const openCreate = () => {
    setEditingTask(null);
    setForm(DEFAULT_FORM);
    setShowModal(true);
  };

  const openEdit = (task) => {
    setEditingTask(task);
    setForm({
      title: task.title || '',
      description: task.description || '',
      status: task.status || 'TODO',
      priority: task.priority || 'MEDIUM',
      assignedRole: task.assignedRole || 'ADMIN',
      dueDate: task.dueDate ? task.dueDate.substring(0, 10) : '',
    });
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) {
      toast.error('Tiêu đề task là bắt buộc');
      return;
    }
    setSaving(true);
    try {
      const payload = { ...form };
      if (!payload.dueDate) delete payload.dueDate;
      if (editingTask) {
        await adminApi.updateTask(editingTask.id, payload);
        toast.success('Cập nhật task thành công');
      } else {
        await adminApi.createTask(payload);
        toast.success('Tạo task thành công');
      }
      setShowModal(false);
      fetchTasks();
      fetchStatistics();
    } catch (err) {
      toast.error(err.message || 'Có lỗi xảy ra');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa task này?')) return;
    setDeletingId(id);
    try {
      await adminApi.deleteTask(id);
      toast.success('Xóa task thành công');
      fetchTasks();
      fetchStatistics();
    } catch (err) {
      toast.error(err.message || 'Không thể xóa task');
    } finally {
      setDeletingId(null);
    }
  };

  const handleStatusChange = async (task, newStatus) => {
    try {
      await adminApi.updateTaskStatus(task.id, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      fetchTasks();
      fetchStatistics();
    } catch (err) {
      toast.error(err.message || 'Không thể cập nhật trạng thái');
    }
  };

  const statCards = [
    { label: 'Tổng task', value: statistics?.total ?? tasks.length, color: 'text-gray-700', bg: 'bg-gray-50' },
    { label: 'Todo', value: statistics?.todo ?? tasks.filter(t => t.status === 'TODO').length, color: 'text-gray-600', bg: 'bg-gray-50' },
    { label: 'Đang làm', value: statistics?.inProgress ?? tasks.filter(t => t.status === 'IN_PROGRESS').length, color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: 'Hoàn thành', value: statistics?.done ?? tasks.filter(t => t.status === 'DONE').length, color: 'text-green-600', bg: 'bg-green-50' },
  ];

  return (
    <div className="space-y-6">
      {/* Statistics */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {statCards.map((card) => (
          <div key={card.label} className={`${card.bg} rounded-lg p-4 border border-gray-200`}>
            <p className="text-sm text-gray-500">{card.label}</p>
            <p className={`text-2xl font-bold ${card.color}`}>{card.value}</p>
          </div>
        ))}
      </div>

      {/* Header */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div className="flex items-center gap-2">
            <CheckSquare className="text-red-500" size={24} />
            <h2 className="text-xl font-bold text-gray-900">Quản lý Task</h2>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setViewMode('list')}
              className={`p-2 rounded border ${viewMode === 'list' ? 'bg-red-50 border-red-300 text-red-600' : 'border-gray-300 text-gray-500 hover:bg-gray-50'}`}
              title="List view"
            >
              <List size={18} />
            </button>
            <button
              onClick={() => setViewMode('kanban')}
              className={`p-2 rounded border ${viewMode === 'kanban' ? 'bg-red-50 border-red-300 text-red-600' : 'border-gray-300 text-gray-500 hover:bg-gray-50'}`}
              title="Kanban view"
            >
              <Grid3X3 size={18} />
            </button>
            <button
              onClick={openCreate}
              className="flex items-center gap-2 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
            >
              <Plus size={18} />
              Tạo task
            </button>
          </div>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-3 items-center">
          <Filter size={16} className="text-gray-400" />
          <div className="flex gap-2 flex-wrap">
            {STATUSES.map(s => (
              <button
                key={s}
                onClick={() => setFilterStatus(s)}
                className={`px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
                  filterStatus === s ? 'bg-red-500 text-white border-red-500' : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
                }`}
              >
                {s === 'ALL' ? 'Tất cả' : s === 'IN_PROGRESS' ? 'Đang làm' : s === 'TODO' ? 'Todo' : s === 'DONE' ? 'Hoàn thành' : 'Huỷ'}
              </button>
            ))}
          </div>
          <div className="flex gap-2 flex-wrap ml-2">
            {PRIORITIES.map(p => (
              <button
                key={p}
                onClick={() => setFilterPriority(p)}
                className={`px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
                  filterPriority === p ? 'bg-gray-800 text-white border-gray-800' : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
                }`}
              >
                {p === 'ALL' ? 'Mọi ưu tiên' : p}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex justify-center py-16">
          <div className="w-8 h-8 border-4 border-red-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : viewMode === 'list' ? (
        <ListView tasks={tasks} onEdit={openEdit} onDelete={handleDelete} onStatusChange={handleStatusChange} deletingId={deletingId} />
      ) : (
        <KanbanView tasks={tasks} onEdit={openEdit} onDelete={handleDelete} onStatusChange={handleStatusChange} />
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg">
            <div className="flex items-center justify-between p-6 border-b">
              <h3 className="text-lg font-bold text-gray-900">
                {editingTask ? 'Chỉnh sửa Task' : 'Tạo Task mới'}
              </h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">
                <XCircle size={24} />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tiêu đề *</label>
                <input
                  type="text"
                  value={form.title}
                  onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder="Nhập tiêu đề task..."
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
                <textarea
                  value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder="Mô tả task..."
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Trạng thái</label>
                  <select
                    value={form.status}
                    onChange={e => setForm(f => ({ ...f, status: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  >
                    <option value="TODO">TODO</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="DONE">DONE</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Ưu tiên</label>
                  <select
                    value={form.priority}
                    onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="URGENT">URGENT</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Phân công cho</label>
                  <select
                    value={form.assignedRole}
                    onChange={e => setForm(f => ({ ...f, assignedRole: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  >
                    {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Hạn hoàn thành</label>
                  <input
                    type="date"
                    value={form.dueDate}
                    onChange={e => setForm(f => ({ ...f, dueDate: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                >
                  Huỷ
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 flex items-center gap-2"
                >
                  {saving && <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                  {editingTask ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function ListView({ tasks, onEdit, onDelete, onStatusChange, deletingId }) {
  if (tasks.length === 0) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-12 text-center text-gray-500">
        <AlertCircle size={40} className="mx-auto mb-3 text-gray-300" />
        <p>Không có task nào</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {['Tiêu đề', 'Trạng thái', 'Ưu tiên', 'Vai trò', 'Hạn', 'Hành động'].map(h => (
                <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {tasks.map(task => {
              return (
                <tr key={task.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <p className="font-medium text-gray-900 text-sm">{task.title}</p>
                    {task.description && <p className="text-xs text-gray-500 mt-0.5 truncate max-w-xs">{task.description}</p>}
                  </td>
                  <td className="px-4 py-3">
                    <select
                      value={task.status}
                      onChange={e => onStatusChange(task, e.target.value)}
                      className={`text-xs px-2 py-1 rounded-full font-medium border-0 cursor-pointer ${STATUS_COLORS[task.status] || 'bg-gray-100 text-gray-700'}`}
                    >
                      <option value="TODO">TODO</option>
                      <option value="IN_PROGRESS">IN_PROGRESS</option>
                      <option value="DONE">DONE</option>
                      <option value="CANCELLED">CANCELLED</option>
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${PRIORITY_COLORS[task.priority] || 'bg-gray-100 text-gray-700'}`}>
                      {task.priority}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">{task.assignedRole || '-'}</td>
                  <td className="px-4 py-3 text-sm text-gray-500">
                    {task.dueDate ? new Date(task.dueDate).toLocaleDateString('vi-VN') : '-'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button onClick={() => onEdit(task)} className="p-1 text-blue-500 hover:text-blue-700" title="Chỉnh sửa">
                        <Edit size={16} />
                      </button>
                      <button
                        onClick={() => onDelete(task.id)}
                        disabled={deletingId === task.id}
                        className="p-1 text-red-500 hover:text-red-700 disabled:opacity-40"
                        title="Xóa"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function KanbanView({ tasks, onEdit, onDelete, onStatusChange }) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {KANBAN_COLUMNS.map(col => {
        const colTasks = tasks.filter(t => t.status === col);
        return (
          <div key={col} className="bg-gray-50 rounded-lg border border-gray-200 p-3">
            <div className="flex items-center justify-between mb-3">
              <span className={`text-xs font-semibold px-2 py-1 rounded-full ${STATUS_COLORS[col]}`}>{col}</span>
              <span className="text-xs text-gray-400">{colTasks.length}</span>
            </div>
            <div className="space-y-2">
              {colTasks.map(task => (
                <div key={task.id} className="bg-white rounded-lg p-3 border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
                  <div className="flex items-start justify-between gap-1 mb-2">
                    <p className="text-sm font-medium text-gray-900 leading-snug flex-1">{task.title}</p>
                    <div className="flex gap-1 shrink-0">
                      <button onClick={() => onEdit(task)} className="p-1 text-blue-400 hover:text-blue-600">
                        <Edit size={13} />
                      </button>
                      <button onClick={() => onDelete(task.id)} className="p-1 text-red-400 hover:text-red-600">
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                  {task.description && (
                    <p className="text-xs text-gray-500 mb-2 line-clamp-2">{task.description}</p>
                  )}
                  <div className="flex items-center justify-between">
                    <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${PRIORITY_COLORS[task.priority] || 'bg-gray-100 text-gray-600'}`}>
                      {task.priority}
                    </span>
                    {task.dueDate && (
                      <span className="text-xs text-gray-400 flex items-center gap-1">
                        <Clock size={10} />
                        {new Date(task.dueDate).toLocaleDateString('vi-VN')}
                      </span>
                    )}
                  </div>
                </div>
              ))}
              {colTasks.length === 0 && (
                <p className="text-xs text-gray-400 text-center py-4">Không có task</p>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default TaskManagement;
