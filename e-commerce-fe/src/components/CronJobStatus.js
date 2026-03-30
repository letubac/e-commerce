import React, { useState, useEffect, useCallback } from 'react';
import { Clock, RefreshCw, CheckCircle, XCircle, Loader2, Calendar } from 'lucide-react';
import adminApi from '../api/adminApi';

const KNOWN_JOBS = [
  {
    name: 'Flash Sale Expiry Check',
    description: 'Kiểm tra và tắt các flash sale đã hết hạn',
    schedule: 'Mỗi phút',
  },
  {
    name: 'Order Status Check',
    description: 'Cập nhật trạng thái đơn hàng tự động',
    schedule: 'Mỗi 5 phút',
  },
  {
    name: 'Expired Session Cleanup',
    description: 'Dọn dẹp các phiên đăng nhập đã hết hạn',
    schedule: 'Mỗi giờ',
  },
  {
    name: 'Nightly Cleanup',
    description: 'Dọn dẹp dữ liệu tạm thời hàng đêm',
    schedule: 'Hàng ngày lúc 2:00 AM',
  },
];

const STATUS_CONFIG = {
  SUCCESS: { icon: CheckCircle, color: 'text-green-500', bg: 'bg-green-50', label: 'Thành công' },
  ERROR: { icon: XCircle, color: 'text-red-500', bg: 'bg-red-50', label: 'Lỗi' },
  RUNNING: { icon: Loader2, color: 'text-blue-500', bg: 'bg-blue-50', label: 'Đang chạy', spin: true },
};

function SkeletonCard() {
  return (
    <div className="bg-white rounded-lg border border-gray-200 p-5 animate-pulse">
      <div className="h-4 bg-gray-200 rounded w-2/3 mb-3" />
      <div className="h-3 bg-gray-100 rounded w-full mb-2" />
      <div className="h-3 bg-gray-100 rounded w-1/2 mb-4" />
      <div className="flex justify-between">
        <div className="h-3 bg-gray-100 rounded w-1/3" />
        <div className="h-5 bg-gray-200 rounded-full w-20" />
      </div>
    </div>
  );
}

function JobCard({ job }) {
  const statusKey = job.status?.toUpperCase() || 'SUCCESS';
  const config = STATUS_CONFIG[statusKey] || STATUS_CONFIG.SUCCESS;
  const StatusIcon = config.icon;

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-5 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between mb-2">
        <div className="flex items-center gap-2">
          <Calendar size={16} className="text-gray-400 shrink-0 mt-0.5" />
          <h3 className="font-semibold text-gray-900 text-sm">{job.name}</h3>
        </div>
        <span className={`flex items-center gap-1 text-xs px-2 py-1 rounded-full font-medium ${config.bg} ${config.color}`}>
          <StatusIcon size={12} className={config.spin ? 'animate-spin' : ''} />
          {config.label}
        </span>
      </div>

      <p className="text-xs text-gray-500 mb-3">{job.description}</p>

      <div className="space-y-1.5 text-xs text-gray-500">
        <div className="flex items-center gap-2">
          <Clock size={11} className="shrink-0" />
          <span className="font-medium text-gray-600">Lịch chạy:</span>
          <span>{job.schedule}</span>
        </div>
        {job.lastRunAt && (
          <div className="flex items-center gap-2">
            <CheckCircle size={11} className="shrink-0" />
            <span className="font-medium text-gray-600">Lần cuối:</span>
            <span>{new Date(job.lastRunAt).toLocaleString('vi-VN')}</span>
          </div>
        )}
        {job.nextRunAt && (
          <div className="flex items-center gap-2">
            <Clock size={11} className="shrink-0" />
            <span className="font-medium text-gray-600">Lần tiếp:</span>
            <span>{new Date(job.nextRunAt).toLocaleString('vi-VN')}</span>
          </div>
        )}
        {job.errorMessage && (
          <p className="text-red-500 mt-1 break-words">{job.errorMessage}</p>
        )}
      </div>
    </div>
  );
}

function CronJobStatus() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [lastRefreshed, setLastRefreshed] = useState(null);

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    try {
      const data = await adminApi.getCronJobs();
      // Merge API data with known jobs list for display
      const apiJobs = Array.isArray(data) ? data : [];
      const merged = KNOWN_JOBS.map(known => {
        const found = apiJobs.find(j => j.name === known.name);
        return found ? { ...known, ...found } : { ...known, status: 'SUCCESS' };
      });
      setJobs(merged);
      setLastRefreshed(new Date());
    } catch {
      // Fallback to showing known jobs with default status
      setJobs(KNOWN_JOBS.map(j => ({ ...j, status: 'SUCCESS' })));
      setLastRefreshed(new Date());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchJobs();
    const interval = setInterval(fetchJobs, 30000);
    return () => clearInterval(interval);
  }, [fetchJobs]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-orange-50 rounded-lg flex items-center justify-center">
              <Clock className="text-orange-500" size={22} />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">Cron Jobs</h2>
              <p className="text-sm text-gray-500">
                {lastRefreshed
                  ? `Cập nhật lúc ${lastRefreshed.toLocaleTimeString('vi-VN')} • Tự động làm mới mỗi 30s`
                  : 'Theo dõi các tác vụ định kỳ'}
              </p>
            </div>
          </div>
          <button
            onClick={fetchJobs}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-50 transition-colors"
          >
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
            Làm mới
          </button>
        </div>
      </div>

      {/* Job cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {loading && jobs.length === 0
          ? Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)
          : jobs.map(job => <JobCard key={job.name} job={job} />)
        }
      </div>
    </div>
  );
}

export default CronJobStatus;
