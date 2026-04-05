/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import {
  Clock, RefreshCw, CheckCircle, XCircle, Loader2, Calendar,
  Power, PowerOff, PauseCircle, PlayCircle
} from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';
import ConfirmDialog, { ACTION_TYPES } from './ConfirmDialog';

// Maps BE job keys → human-readable metadata
const JOB_META = {
  syncFlashSaleStatus: {
    label: 'Flash Sale Sync',
    description: 'Tự động bật/tắt flash sale theo lịch & đánh dấu sản phẩm hết hàng',
    schedule: 'Mỗi 10 giây',
  },
  checkFlashSaleExpiry: {
    label: 'Coupon Expiry Check',
    description: 'Tắt coupon hết hạn hoặc đã dùng hết',
    schedule: 'Mỗi phút',
  },
  checkOrderStatuses: {
    label: 'Order Status Check',
    description: 'Cảnh báo & tự hủy đơn hàng PENDING quá hạn',
    schedule: 'Mỗi 5 phút',
  },
  cleanExpiredSessions: {
    label: 'Expired Session Cleanup',
    description: 'Xóa thông báo đã hết hạn (expires_at < now)',
    schedule: 'Mỗi giờ',
  },
  nightlyCleanup: {
    label: 'Nightly Cleanup',
    description: 'Xóa thông báo cũ > 90 ngày & deactivate coupon hết hạn',
    schedule: 'Hàng ngày lúc 2:00 AM',
  },
};

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

function JobCard({ jobKey, jobData, onToggle, onPause, onResume, actionLoading }) {
  const meta = JOB_META[jobKey] || { label: jobKey, description: '', schedule: 'N/A' };
  const statusKey = jobData?.status?.toUpperCase() || 'SUCCESS';
  const config = STATUS_CONFIG[statusKey] || STATUS_CONFIG.SUCCESS;
  const StatusIcon = config.icon;

  const isEnabled = jobData?.enabled !== false;
  const isPaused = jobData?.pausedUntil != null;

  let runState = 'active';
  if (!isEnabled) runState = 'disabled';
  else if (isPaused) runState = 'paused';

  const runStateConfig = {
    active: { dot: 'bg-green-400', text: 'Đang chạy' },
    disabled: { dot: 'bg-gray-400', text: 'Đã tắt' },
    paused: { dot: 'bg-yellow-400', text: 'Tạm ngừng' },
  };

  const loading = actionLoading === jobKey;

  return (
    <div className={`bg-white rounded-lg border-2 p-5 transition-all ${
      runState === 'disabled' ? 'border-gray-200 opacity-70' :
      runState === 'paused' ? 'border-yellow-200' : 'border-gray-200 hover:shadow-md'
    }`}>
      {/* Title row */}
      <div className="flex items-start justify-between mb-2 gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <Calendar size={15} className="text-gray-400 shrink-0 mt-0.5" />
          <h3 className="font-semibold text-gray-900 text-sm truncate">{meta.label}</h3>
        </div>
        <div className="flex items-center gap-1.5 flex-shrink-0">
          <span className={`inline-block w-2 h-2 rounded-full ${runStateConfig[runState].dot}`} />
          <span className="text-xs text-gray-500">{runStateConfig[runState].text}</span>
        </div>
      </div>

      <p className="text-xs text-gray-500 mb-3">{meta.description}</p>

      {/* Schedule / last run info */}
      <div className="space-y-1 text-xs text-gray-500 mb-4">
        <div className="flex items-center gap-2">
          <Clock size={11} className="shrink-0" />
          <span className="font-medium text-gray-600">Lịch chạy:</span>
          <span>{meta.schedule}</span>
        </div>
        {jobData?.lastRun && (
          <div className="flex items-center gap-2">
            <CheckCircle size={11} className="shrink-0" />
            <span className="font-medium text-gray-600">Lần cuối:</span>
            <span>{new Date(jobData.lastRun).toLocaleString('vi-VN')}</span>
          </div>
        )}
        {jobData?.message && (
          <p className={`mt-1 break-words ${statusKey === 'ERROR' ? 'text-red-500' : 'text-gray-400'}`}>
            {jobData.message}
          </p>
        )}
        {isPaused && (
          <p className="text-yellow-600 mt-1">
            Tạm ngừng đến: {new Date(jobData.pausedUntil).toLocaleString('vi-VN')}
          </p>
        )}
      </div>

      {/* Status badge */}
      <div className="flex items-center justify-between mb-3">
        <span className={`flex items-center gap-1 text-xs px-2 py-1 rounded-full font-medium ${config.bg} ${config.color}`}>
          <StatusIcon size={12} className={config.spin ? 'animate-spin' : ''} />
          {config.label}
        </span>
      </div>

      {/* Action buttons */}
      <div className="flex gap-1.5">
        <button
          onClick={() => onToggle(jobKey, isEnabled)}
          disabled={loading}
          title={isEnabled ? 'Tắt job này' : 'Bật job này'}
          className={`flex-1 flex items-center justify-center gap-1 px-2 py-1.5 text-xs rounded border transition-colors ${
            isEnabled
              ? 'border-red-200 text-red-600 hover:bg-red-50'
              : 'border-green-200 text-green-600 hover:bg-green-50'
          } disabled:opacity-50`}
        >
          {isEnabled ? <PowerOff size={12} /> : <Power size={12} />}
          {isEnabled ? 'Tắt' : 'Bật'}
        </button>

        {isEnabled && !isPaused && (
          <button
            onClick={() => onPause(jobKey)}
            disabled={loading}
            title="Tạm ngừng 60 phút"
            className="flex-1 flex items-center justify-center gap-1 px-2 py-1.5 text-xs rounded border border-yellow-200 text-yellow-600 hover:bg-yellow-50 disabled:opacity-50 transition-colors"
          >
            <PauseCircle size={12} />
            Dừng
          </button>
        )}

        {isPaused && (
          <button
            onClick={() => onResume(jobKey)}
            disabled={loading}
            title="Tiếp tục ngay"
            className="flex-1 flex items-center justify-center gap-1 px-2 py-1.5 text-xs rounded border border-blue-200 text-blue-600 hover:bg-blue-50 disabled:opacity-50 transition-colors"
          >
            <PlayCircle size={12} />
            Tiếp tục
          </button>
        )}

        {loading && (
          <div className="flex items-center justify-center px-2">
            <Loader2 size={14} className="animate-spin text-gray-400" />
          </div>
        )}
      </div>
    </div>
  );
}

function CronJobStatus() {
  const [jobsMap, setJobsMap] = useState({});
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);
  const [lastRefreshed, setLastRefreshed] = useState(null);
  // confirmDialog state: { isOpen, jobKey, actionType, confirmFn }
  const [confirm, setConfirm] = useState({ isOpen: false });

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    try {
      const data = await adminApi.getCronJobs();
      setJobsMap(data && typeof data === 'object' ? data : {});
      setLastRefreshed(new Date());
    } catch {
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

  const handleToggle = (jobKey, currentlyEnabled) => {
    setConfirm({
      isOpen: true,
      jobKey,
      actionType: currentlyEnabled ? ACTION_TYPES.DEACTIVATE : ACTION_TYPES.ACTIVATE,
      detail: currentlyEnabled
        ? `Job "${JOB_META[jobKey]?.label || jobKey}" sẽ bị tắt và không chạy nữa.`
        : `Job "${JOB_META[jobKey]?.label || jobKey}" sẽ được bật lại.`,
      confirmFn: async () => {
        setActionLoading(jobKey);
        try {
          await adminApi.toggleCronJob(jobKey);
          // Optimistic update — card reflects new state immediately with the toast
          setJobsMap(prev => ({
            ...prev,
            [jobKey]: { ...(prev[jobKey] || {}), enabled: !currentlyEnabled }
          }));
          toast.success(currentlyEnabled ? 'Đã tắt job' : 'Đã bật job');
          await fetchJobs();
        } catch (error) {
          // Revert optimistic update on failure
          setJobsMap(prev => ({
            ...prev,
            [jobKey]: { ...(prev[jobKey] || {}), enabled: currentlyEnabled }
          }));
          toast.error(error.message || 'Không thể thay đổi trạng thái job');
        } finally {
          setActionLoading(null);
        }
      }
    });
  };

  const handlePause = (jobKey) => {
    setConfirm({
      isOpen: true,
      jobKey,
      actionType: ACTION_TYPES.PAUSE,
      detail: `Job "${JOB_META[jobKey]?.label || jobKey}" sẽ bị tạm ngừng trong 60 phút.`,
      confirmFn: async () => {
        setActionLoading(jobKey);
        try {
          await adminApi.pauseCronJob(jobKey, 60);
          // Optimistic update — show paused state immediately
          const pausedUntil = new Date(Date.now() + 60 * 60 * 1000).toISOString();
          setJobsMap(prev => ({
            ...prev,
            [jobKey]: { ...(prev[jobKey] || {}), pausedUntil }
          }));
          toast.success('Đã tạm ngừng job trong 60 phút');
          await fetchJobs();
        } catch (error) {
          toast.error(error.message || 'Không thể tạm ngừng job');
        } finally {
          setActionLoading(null);
        }
      }
    });
  };

  const handleResume = async (jobKey) => {
    setActionLoading(jobKey);
    try {
      await adminApi.resumeCronJob(jobKey);
      // Optimistic update — remove paused state immediately
      setJobsMap(prev => {
        const updated = { ...(prev[jobKey] || {}) };
        delete updated.pausedUntil;
        return { ...prev, [jobKey]: updated };
      });
      toast.success('Đã tiếp tục job');
      await fetchJobs();
    } catch (error) {
      toast.error(error.message || 'Không thể tiếp tục job');
    } finally {
      setActionLoading(null);
    }
  };

  const handleConfirm = async () => {
    if (confirm.confirmFn) {
      await confirm.confirmFn();
    }
    setConfirm({ isOpen: false });
  };

  const jobKeys = Object.keys(JOB_META);

  return (
    <>
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
                    ? `Cập nhật lúc ${lastRefreshed.toLocaleTimeString('vi-VN')} · Tự động làm mới mỗi 30s`
                    : 'Theo dõi và kiểm soát các tác vụ định kỳ'}
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
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-4 gap-4">
          {loading && Object.keys(jobsMap).length === 0
            ? Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)
            : jobKeys.map(jobKey => (
                <JobCard
                  key={jobKey}
                  jobKey={jobKey}
                  jobData={jobsMap[jobKey]}
                  onToggle={handleToggle}
                  onPause={handlePause}
                  onResume={handleResume}
                  actionLoading={actionLoading}
                />
              ))
          }
        </div>
      </div>

      <ConfirmDialog
        isOpen={confirm.isOpen}
        action={confirm.actionType || ACTION_TYPES.STATUS_CHANGE}
        target={JOB_META[confirm.jobKey]?.label || confirm.jobKey || 'job'}
        detail={confirm.detail}
        onConfirm={handleConfirm}
        onCancel={() => setConfirm({ isOpen: false })}
        loading={!!actionLoading}
      />
    </>
  );
}

export default CronJobStatus;
