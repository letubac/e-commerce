/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Calendar, Clock, Package, Plus, ChevronRight, Zap, CheckCircle, AlertCircle, ChevronLeft, LayoutGrid } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

// ─── helpers ────────────────────────────────────────────────────────────────

const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

const formatDuration = (startStr, endStr) => {
  const diff = new Date(endStr) - new Date(startStr);
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  return h > 0 ? `${h}h${m > 0 ? ` ${m}m` : ''}` : `${m}m`;
};

const getTimeUntilStart = (startStr, now) => {
  const diff = new Date(startStr) - now;
  if (diff <= 0) return null;
  const d = Math.floor(diff / 86400000);
  const h = Math.floor((diff % 86400000) / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  if (d > 0) return `Còn ${d} ngày ${h}h`;
  if (h > 0) return `Còn ${h}h ${m}m`;
  return `Còn ${m} phút`;
};

const MONTH_NAMES = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
  'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
const DOW = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];

// ─── Calendar sub-component ──────────────────────────────────────────────────

function CalendarView({ flashSales, onManageProducts }) {
  const today = new Date();
  const [calYear, setCalYear] = useState(today.getFullYear());
  const [calMonth, setCalMonth] = useState(today.getMonth()); // 0-indexed
  const [selectedDay, setSelectedDay] = useState(null);

  const firstDayOfMonth = new Date(calYear, calMonth, 1);
  const daysInMonth = new Date(calYear, calMonth + 1, 0).getDate();
  const startDow = firstDayOfMonth.getDay(); // 0=Sunday

  // Build cells (including padding days from prev month)
  const totalCells = Math.ceil((startDow + daysInMonth) / 7) * 7;
  const cells = [];
  for (let i = 0; i < totalCells; i++) {
    const dayNum = i - startDow + 1;
    if (dayNum < 1 || dayNum > daysInMonth) {
      cells.push(null);
    } else {
      cells.push(dayNum);
    }
  }

  // For each day, find flash sales that overlap it
  const salesOnDay = (dayNum) => {
    if (!dayNum) return [];
    const dayStart = new Date(calYear, calMonth, dayNum, 0, 0, 0);
    const dayEnd = new Date(calYear, calMonth, dayNum, 23, 59, 59);
    return flashSales.filter(fs => {
      const s = new Date(fs.startTime);
      const e = new Date(fs.endTime);
      return s <= dayEnd && e >= dayStart;
    });
  };

  const prevMonth = () => {
    if (calMonth === 0) { setCalMonth(11); setCalYear(y => y - 1); }
    else setCalMonth(m => m - 1);
    setSelectedDay(null);
  };
  const nextMonth = () => {
    if (calMonth === 11) { setCalMonth(0); setCalYear(y => y + 1); }
    else setCalMonth(m => m + 1);
    setSelectedDay(null);
  };

  const selectedSales = selectedDay ? salesOnDay(selectedDay) : [];
  const isToday = (dayNum) =>
    dayNum && today.getDate() === dayNum && today.getMonth() === calMonth && today.getFullYear() === calYear;

  return (
    <div className="space-y-4">
      {/* Calendar header */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 bg-gradient-to-r from-red-50 to-orange-50 border-b border-gray-100">
          <button
            onClick={prevMonth}
            className="p-2 rounded-lg hover:bg-white hover:shadow transition text-gray-600"
          >
            <ChevronLeft size={20} />
          </button>
          <h3 className="text-lg font-bold text-gray-800">
            {MONTH_NAMES[calMonth]} {calYear}
          </h3>
          <button
            onClick={nextMonth}
            className="p-2 rounded-lg hover:bg-white hover:shadow transition text-gray-600"
          >
            <ChevronRight size={20} />
          </button>
        </div>

        {/* DOW header */}
        <div className="grid grid-cols-7 border-b border-gray-100">
          {DOW.map(d => (
            <div key={d} className="py-2 text-center text-xs font-semibold text-gray-500 uppercase tracking-wide">
              {d}
            </div>
          ))}
        </div>

        {/* Day grid */}
        <div className="grid grid-cols-7">
          {cells.map((dayNum, idx) => {
            const sales = salesOnDay(dayNum);
            const isSelected = selectedDay === dayNum;
            return (
              <div
                key={idx}
                onClick={() => dayNum && setSelectedDay(isSelected ? null : dayNum)}
                className={`min-h-[80px] border-b border-r border-gray-50 p-1.5 transition
                  ${!dayNum ? 'bg-gray-50/50' : 'cursor-pointer hover:bg-red-50/40'}
                  ${isToday(dayNum) ? 'bg-amber-50' : ''}
                  ${isSelected ? 'ring-2 ring-inset ring-red-400 bg-red-50' : ''}
                `}
              >
                {dayNum && (
                  <>
                    <div className={`text-sm font-medium mb-1 w-6 h-6 flex items-center justify-center rounded-full
                      ${isToday(dayNum) ? 'bg-red-500 text-white' : 'text-gray-700'}`}>
                      {dayNum}
                    </div>
                    <div className="space-y-0.5">
                      {sales.slice(0, 3).map(fs => (
                        <div
                          key={fs.id}
                          className="text-[10px] leading-tight px-1 py-0.5 rounded truncate text-white font-medium"
                          style={{ backgroundColor: fs.backgroundColor || '#EF4444' }}
                          title={fs.name}
                        >
                          {fs.name}
                        </div>
                      ))}
                      {sales.length > 3 && (
                        <div className="text-[10px] text-gray-400 pl-1">+{sales.length - 3} thêm</div>
                      )}
                    </div>
                  </>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Day detail panel */}
      {selectedDay && (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4">
          <h4 className="font-bold text-gray-800 mb-3">
            Ngày {selectedDay}/{calMonth + 1}/{calYear}
            <span className="ml-2 text-sm font-normal text-gray-500">
              {selectedSales.length === 0 ? '— Không có Flash Sale' : `${selectedSales.length} Flash Sale`}
            </span>
          </h4>
          {selectedSales.length === 0 ? (
            <p className="text-gray-400 text-sm py-2">Không có Flash Sale nào trong ngày này.</p>
          ) : (
            <div className="space-y-3">
              {selectedSales.map(fs => (
                <div
                  key={fs.id}
                  className="flex items-center gap-3 p-3 rounded-lg border border-gray-100 hover:bg-gray-50 cursor-pointer transition"
                  onClick={() => onManageProducts(fs)}
                >
                  <div className="w-2.5 h-10 rounded-full flex-shrink-0" style={{ backgroundColor: fs.backgroundColor || '#EF4444' }} />
                  <div className="flex-1 min-w-0">
                    <div className="font-semibold text-gray-900 text-sm truncate">{fs.name}</div>
                    <div className="text-xs text-gray-500 mt-0.5">
                      {formatDateTime(fs.startTime)} → {formatDateTime(fs.endTime)}
                    </div>
                  </div>
                  <div className="flex items-center gap-2 text-xs flex-shrink-0">
                    <span className={`px-2 py-0.5 rounded-full font-medium
                      ${fs.isActive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {fs.isActive ? 'Active' : 'Inactive'}
                    </span>
                    <ChevronRight size={14} className="text-gray-400" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ─── Card sub-components ─────────────────────────────────────────────────────

function SectionHeader({ icon: Icon, title, count, color }) {
  return (
    <div className="flex items-center gap-3 mb-4">
      <div className={`p-2 rounded-lg ${color}`}>
        <Icon size={20} className="text-white" />
      </div>
      <h3 className="text-lg font-bold text-gray-800">{title}</h3>
      <span className="px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full text-sm font-medium">{count}</span>
    </div>
  );
}

function SaleCard({ fs, variant, now, onManageProducts }) {
  const isOngoing = variant === 'ongoing';
  const isPast = variant === 'past';

  return (
    <div
      className={`relative bg-white rounded-xl border-2 p-5 transition hover:shadow-lg cursor-pointer group
        ${isOngoing ? 'border-green-400 shadow-green-50 shadow-md' : ''}
        ${isPast ? 'border-gray-200 opacity-70' : ''}
        ${!isOngoing && !isPast ? 'border-blue-200 hover:border-blue-400' : ''}`}
      onClick={() => onManageProducts(fs)}
    >
      <div className="absolute top-0 left-0 w-1 h-full rounded-l-xl"
        style={{ backgroundColor: fs.backgroundColor || '#EF4444' }} />

      <div className="flex items-start justify-between mb-3 pl-2">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            {isOngoing && (
              <span className="flex items-center gap-1 px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">
                <span className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse" />
                Đang diễn ra
              </span>
            )}
            {!isOngoing && !isPast && (
              <span className="flex items-center gap-1 px-2 py-0.5 bg-blue-100 text-blue-700 rounded-full text-xs font-medium">
                <Clock size={10} />
                {getTimeUntilStart(fs.startTime, now)}
              </span>
            )}
            {isPast && (
              <span className="px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-xs">Đã kết thúc</span>
            )}
            {!fs.isActive && !isPast && (
              <span className="px-2 py-0.5 bg-yellow-100 text-yellow-700 rounded-full text-xs">Chưa kích hoạt</span>
            )}
          </div>
          <h4 className="font-bold text-gray-900 text-base">{fs.name}</h4>
          {fs.description && <p className="text-sm text-gray-500 mt-0.5 line-clamp-1">{fs.description}</p>}
        </div>
        <div
          className={`p-2 rounded-lg opacity-0 group-hover:opacity-100 transition ${isPast ? 'hidden' : 'bg-blue-50 text-blue-600'}`}
          title="Quản lý sản phẩm"
          onClick={(e) => { e.stopPropagation(); onManageProducts(fs); }}
        >
          <ChevronRight size={18} />
        </div>
      </div>

      <div className="pl-2 flex flex-col gap-1 mb-3">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <Calendar size={14} className="text-gray-400" />
          <span>{formatDateTime(fs.startTime)}</span>
          <span className="text-gray-300">→</span>
          <span>{formatDateTime(fs.endTime)}</span>
        </div>
        <div className="flex items-center gap-2 text-xs text-gray-400">
          <Clock size={12} />
          <span>Thời lượng: {formatDuration(fs.startTime, fs.endTime)}</span>
        </div>
      </div>

      <div className="pl-2 flex items-center gap-4 pt-3 border-t border-gray-100">
        <button
          onClick={(e) => { e.stopPropagation(); onManageProducts(fs); }}
          className={`flex items-center gap-1.5 text-sm font-medium transition
            ${fs.totalProducts > 0 ? 'text-blue-600 hover:text-blue-800' : 'text-gray-400 hover:text-gray-600'}`}
        >
          <Package size={15} />
          <span>{fs.totalProducts || 0} sản phẩm</span>
        </button>
        {fs.totalProducts === 0 && !isPast && (
          <button
            onClick={(e) => { e.stopPropagation(); onManageProducts(fs); }}
            className="flex items-center gap-1 text-xs text-orange-500 hover:text-orange-700 transition"
          >
            <AlertCircle size={13} />
            <span>Chưa có sản phẩm</span>
          </button>
        )}
        {fs.totalSales > 0 && (
          <span className="flex items-center gap-1 text-xs text-gray-500 ml-auto">
            <Zap size={12} className="text-yellow-500" />
            {fs.totalSales} đã bán
          </span>
        )}
      </div>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

function FlashSaleScheduleView({ onCreateNew, onManageProducts, onRefresh }) {
  const [flashSales, setFlashSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState('cards'); // 'cards' | 'calendar'

  useEffect(() => {
    fetchSchedule();
  }, []);

  useEffect(() => {
    if (onRefresh) fetchSchedule();
  }, [onRefresh]);

  const fetchSchedule = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getFlashSales({ page: 0, size: 200, sortBy: 'startTime', sortDirection: 'asc' });
      const sales = Array.isArray(data) ? data : (data?.content || []);
      setFlashSales(sales);
    } catch {
      toast.error('Lỗi khi tải lịch Flash Sale');
    } finally {
      setLoading(false);
    }
  };

  const now = new Date();
  const ongoing = flashSales.filter(fs => {
    const s = new Date(fs.startTime), e = new Date(fs.endTime);
    return s <= now && e > now;
  });
  const upcoming = flashSales.filter(fs => new Date(fs.startTime) > now && !fs.expired);
  const past = flashSales.filter(fs => new Date(fs.endTime) <= now || fs.expired);

  if (loading) {
    return (
      <div className="text-center py-16">
        <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-red-600" />
        <p className="mt-4 text-gray-500">Đang tải lịch Flash Sale...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* View mode toggle */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">
          {flashSales.length} Flash Sale tổng cộng
        </p>
        <div className="flex items-center gap-1 bg-gray-100 p-1 rounded-lg">
          <button
            onClick={() => setViewMode('cards')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition ${
              viewMode === 'cards' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            <LayoutGrid size={15} />
            Thẻ
          </button>
          <button
            onClick={() => setViewMode('calendar')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition ${
              viewMode === 'calendar' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            <Calendar size={15} />
            Lịch
          </button>
        </div>
      </div>

      {/* Calendar view */}
      {viewMode === 'calendar' && (
        <CalendarView flashSales={flashSales} onManageProducts={onManageProducts} />
      )}

      {/* Cards view */}
      {viewMode === 'cards' && (
        <>
          {flashSales.length === 0 && (
            <div className="text-center py-20">
              <Calendar size={56} className="mx-auto text-gray-300 mb-4" />
              <p className="text-gray-500 text-lg mb-2">Chưa có Flash Sale nào được lên lịch</p>
              <p className="text-gray-400 mb-6">Bấm "Lên lịch mới" để tạo chương trình Flash Sale đầu tiên</p>
              <button
                onClick={onCreateNew}
                className="flex items-center gap-2 px-6 py-3 bg-red-600 text-white rounded-xl hover:bg-red-700 transition mx-auto"
              >
                <Plus size={20} />
                Lên lịch Flash Sale mới
              </button>
            </div>
          )}

          {ongoing.length > 0 && (
            <section>
              <SectionHeader icon={Zap} title="Đang diễn ra" count={ongoing.length} color="bg-green-500" />
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                {ongoing.map(fs => <SaleCard key={fs.id} fs={fs} variant="ongoing" now={now} onManageProducts={onManageProducts} />)}
              </div>
            </section>
          )}

          {upcoming.length > 0 && (
            <section>
              <SectionHeader icon={Clock} title="Sắp diễn ra" count={upcoming.length} color="bg-blue-500" />
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                {upcoming.map(fs => <SaleCard key={fs.id} fs={fs} variant="upcoming" now={now} onManageProducts={onManageProducts} />)}
              </div>
            </section>
          )}

          {upcoming.length === 0 && ongoing.length === 0 && flashSales.length > 0 && (
            <div className="bg-blue-50 border border-blue-200 rounded-xl p-6 flex items-center gap-4">
              <div className="p-3 bg-blue-100 rounded-lg">
                <Plus size={24} className="text-blue-600" />
              </div>
              <div className="flex-1">
                <p className="font-semibold text-blue-800">Không có Flash Sale nào đang chạy hoặc sắp diễn ra</p>
                <p className="text-blue-600 text-sm mt-0.5">Lên lịch ngay để thu hút khách hàng</p>
              </div>
              <button
                onClick={onCreateNew}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium whitespace-nowrap"
              >
                Lên lịch mới
              </button>
            </div>
          )}

          {past.length > 0 && (
            <section>
              <SectionHeader icon={CheckCircle} title="Đã kết thúc" count={past.length} color="bg-gray-400" />
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                {past.map(fs => <SaleCard key={fs.id} fs={fs} variant="past" now={now} onManageProducts={onManageProducts} />)}
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}

export default FlashSaleScheduleView;

