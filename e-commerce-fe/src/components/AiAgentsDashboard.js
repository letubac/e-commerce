/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  Bot, Send, Loader2, MessageSquare, Sparkles,
  BarChart3, Package, ShoppingCart, Megaphone, Network
} from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

// ─── Agent definitions ────────────────────────────────────────────────────────

const AGENTS = [
  {
    key: 'orchestrator',
    label: '🧠 Orchestrator',
    icon: Network,
    color: 'purple',
    description: 'Tự động định tuyến đến agent phù hợp nhất',
    statusKey: 'orchestrator',
    apiCall: (q, sid) => adminApi.chatOrchestratorAgent(q, sid),
    suggestedQuestions: [
      'Phân tích tổng quan kinh doanh hôm nay',
      'Sản phẩm nào cần nhập hàng gấp?',
      'Chiến dịch flash sale nào hiệu quả nhất?',
      'Doanh thu tuần này so với tuần trước',
      'Có bao nhiêu đơn hàng đang chờ xử lý?',
    ],
  },
  {
    key: 'analytics',
    label: '📊 Analytics',
    icon: BarChart3,
    color: 'indigo',
    description: 'Phân tích doanh thu, xu hướng và báo cáo',
    statusKey: 'analytics',
    apiCall: (q, sid) => adminApi.chatAnalytics(q, sid),
    suggestedQuestions: [
      'Tổng quan doanh số hôm nay',
      'Thống kê đơn hàng theo trạng thái',
      'Người dùng mới trong 7 ngày qua',
      'Sản phẩm bán chạy nhất tháng này',
      'Tình trạng task hiện tại',
    ],
  },
  {
    key: 'inventory',
    label: '📦 Inventory',
    icon: Package,
    color: 'orange',
    description: 'Cảnh báo tồn kho và đề xuất nhập hàng',
    statusKey: 'inventory',
    apiCall: (q, sid) => adminApi.chatInventoryAgent(q, sid),
    suggestedQuestions: [
      'Sản phẩm nào đang sắp hết hàng?',
      'Tổng số sản phẩm tồn kho thấp',
      'Đề xuất nhập hàng ưu tiên',
      'Thống kê tồn kho toàn bộ',
      'Top 10 sản phẩm cần nhập gấp',
    ],
  },
  {
    key: 'sales',
    label: '🛒 Sales',
    icon: ShoppingCart,
    color: 'green',
    description: 'Phân tích doanh số và tư vấn tăng trưởng',
    statusKey: 'sales',
    apiCall: (q, sid) => adminApi.chatSalesAgent(q, sid),
    suggestedQuestions: [
      'Doanh thu 30 ngày gần nhất',
      'Top 5 sản phẩm bán chạy nhất',
      'Phân tích tỷ lệ hoàn thành đơn hàng',
      'Tổng quan hiệu quả bán hàng',
      'Gợi ý để tăng doanh số',
    ],
  },
  {
    key: 'marketing',
    label: '🎯 Marketing',
    icon: Megaphone,
    color: 'red',
    description: 'Flash sale, coupon và chiến dịch khuyến mãi',
    statusKey: 'marketing',
    apiCall: (q, sid) => adminApi.chatMarketingAgent(q, sid),
    suggestedQuestions: [
      'Flash sale nào đang hoạt động?',
      'Flash sale sắp diễn ra',
      'Danh sách coupon đang có hiệu lực',
      'Đề xuất chiến dịch khuyến mãi mới',
      'Phân tích hiệu quả coupon',
    ],
  },
];

// ─── Color palette per agent ──────────────────────────────────────────────────

const COLOR_MAP = {
  purple: {
    header: 'from-purple-50 to-pink-50',
    iconBg: 'bg-purple-100',
    iconText: 'text-purple-600',
    userBubble: 'bg-purple-600',
    pill: 'bg-green-100 text-green-700',
    pillOff: 'bg-red-100 text-red-700',
    suggestion: 'bg-purple-50 text-purple-700 border-purple-200 hover:bg-purple-100',
    send: 'bg-purple-600 hover:bg-purple-700',
    tab: 'border-purple-500 text-purple-600',
  },
  indigo: {
    header: 'from-indigo-50 to-purple-50',
    iconBg: 'bg-indigo-100',
    iconText: 'text-indigo-600',
    userBubble: 'bg-indigo-600',
    pill: 'bg-green-100 text-green-700',
    pillOff: 'bg-red-100 text-red-700',
    suggestion: 'bg-indigo-50 text-indigo-700 border-indigo-200 hover:bg-indigo-100',
    send: 'bg-indigo-600 hover:bg-indigo-700',
    tab: 'border-indigo-500 text-indigo-600',
  },
  orange: {
    header: 'from-orange-50 to-amber-50',
    iconBg: 'bg-orange-100',
    iconText: 'text-orange-600',
    userBubble: 'bg-orange-500',
    pill: 'bg-green-100 text-green-700',
    pillOff: 'bg-red-100 text-red-700',
    suggestion: 'bg-orange-50 text-orange-700 border-orange-200 hover:bg-orange-100',
    send: 'bg-orange-500 hover:bg-orange-600',
    tab: 'border-orange-500 text-orange-600',
  },
  green: {
    header: 'from-green-50 to-emerald-50',
    iconBg: 'bg-green-100',
    iconText: 'text-green-600',
    userBubble: 'bg-green-600',
    pill: 'bg-green-100 text-green-700',
    pillOff: 'bg-red-100 text-red-700',
    suggestion: 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100',
    send: 'bg-green-600 hover:bg-green-700',
    tab: 'border-green-500 text-green-600',
  },
  red: {
    header: 'from-red-50 to-rose-50',
    iconBg: 'bg-red-100',
    iconText: 'text-red-600',
    userBubble: 'bg-red-500',
    pill: 'bg-green-100 text-green-700',
    pillOff: 'bg-red-100 text-red-700',
    suggestion: 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100',
    send: 'bg-red-500 hover:bg-red-600',
    tab: 'border-red-500 text-red-600',
  },
};

// ─── Session ID helper ────────────────────────────────────────────────────────

function generateSessionId(agentKey) {
  return `${agentKey}_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

// ─── Single agent chat panel ──────────────────────────────────────────────────

function AgentChatPanel({ agent, agentStatus }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const sessionId = useRef(generateSessionId(agent.key));
  const messagesEndRef = useRef(null);
  const colors = COLOR_MAP[agent.color];
  const IconComponent = agent.icon;

  const isEnabled = agentStatus?.[agent.statusKey]?.enabled ?? false;

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const sendMessage = useCallback(async (text) => {
    const messageText = (text || input).trim();
    if (!messageText || loading) return;

    const userMsg = { role: 'user', content: messageText, id: Date.now() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const data = await agent.apiCall(messageText, sessionId.current);
      const aiMsg = {
        role: 'ai',
        content: data?.reply || 'Không có phản hồi',
        id: Date.now() + 1,
      };
      setMessages(prev => [...prev, aiMsg]);
    } catch (err) {
      const errMsg = {
        role: 'ai',
        content: `❌ Lỗi: ${err.message || 'Không thể kết nối đến AI Agent'}`,
        id: Date.now() + 1,
        isError: true,
      };
      setMessages(prev => [...prev, errMsg]);
      toast.error(err.message || `Lỗi khi gọi ${agent.label}`);
    } finally {
      setLoading(false);
    }
  }, [input, loading, agent]);

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="flex flex-col h-full min-h-[580px] bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      {/* Header */}
      <div className={`flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-gradient-to-r ${colors.header}`}>
        <div className="flex items-center gap-3">
          <div className={`w-10 h-10 ${colors.iconBg} rounded-full flex items-center justify-center`}>
            <IconComponent className={colors.iconText} size={22} />
          </div>
          <div>
            <h2 className="font-bold text-gray-900 flex items-center gap-2">
              {agent.label}
              <Sparkles size={14} className={colors.iconText} />
            </h2>
            <p className="text-xs text-gray-500">{agent.description}</p>
          </div>
        </div>
        {agentStatus === null ? (
          <Loader2 size={16} className="animate-spin text-gray-400" />
        ) : (
          <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${isEnabled ? colors.pill : colors.pillOff}`}>
            {isEnabled ? '● Đang hoạt động' : '● Không khả dụng'}
          </span>
        )}
      </div>

      {/* Not-enabled warning */}
      {agentStatus !== null && !isEnabled && (
        <div className="mx-4 mt-4 p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-center gap-2">
          <Bot size={16} className="text-amber-500 shrink-0" />
          <p className="text-sm text-amber-700">
            Agent chưa được kích hoạt. Cấu hình <code className="font-mono bg-amber-100 px-1 rounded">OPENAI_API_KEY</code> và bật <code className="font-mono bg-amber-100 px-1 rounded">app.ai.enabled=true</code>.
          </p>
        </div>
      )}

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full py-12 text-center">
            <MessageSquare size={48} className="text-gray-200 mb-4" />
            <p className="text-gray-500 font-medium mb-1">Bắt đầu cuộc trò chuyện</p>
            <p className="text-xs text-gray-400">Chọn câu hỏi gợi ý hoặc nhập câu hỏi của bạn</p>
          </div>
        ) : (
          messages.map(msg => (
            <div key={msg.id} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              {msg.role === 'ai' && (
                <div className={`w-7 h-7 ${colors.iconBg} rounded-full flex items-center justify-center mr-2 mt-0.5 shrink-0`}>
                  <IconComponent size={14} className={colors.iconText} />
                </div>
              )}
              <div className={`max-w-[75%] px-4 py-2.5 rounded-2xl text-sm whitespace-pre-wrap ${
                msg.role === 'user'
                  ? `${colors.userBubble} text-white rounded-br-sm`
                  : msg.isError
                    ? 'bg-red-50 text-red-700 border border-red-200 rounded-bl-sm'
                    : 'bg-gray-100 text-gray-800 rounded-bl-sm'
              }`}>
                {msg.content}
              </div>
            </div>
          ))
        )}
        {loading && (
          <div className="flex justify-start">
            <div className={`w-7 h-7 ${colors.iconBg} rounded-full flex items-center justify-center mr-2 mt-0.5 shrink-0`}>
              <IconComponent size={14} className={colors.iconText} />
            </div>
            <div className="bg-gray-100 px-4 py-3 rounded-2xl rounded-bl-sm flex items-center gap-2">
              <Loader2 size={14} className="animate-spin text-gray-500" />
              <span className="text-sm text-gray-500">Đang xử lý...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Suggested questions */}
      {messages.length === 0 && (
        <div className="px-4 pb-3">
          <p className="text-xs text-gray-400 mb-2 font-medium">Gợi ý câu hỏi:</p>
          <div className="flex flex-wrap gap-2">
            {agent.suggestedQuestions.map(q => (
              <button
                key={q}
                onClick={() => sendMessage(q)}
                disabled={loading}
                className={`text-xs px-3 py-1.5 border rounded-full transition-colors disabled:opacity-50 ${colors.suggestion}`}
              >
                {q}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Input */}
      <div className="border-t border-gray-200 p-4">
        <div className="flex gap-2">
          <textarea
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={`Hỏi ${agent.label}... (Enter để gửi)`}
            rows={1}
            className="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-gray-400 resize-none"
            style={{ minHeight: '42px', maxHeight: '120px' }}
            disabled={loading}
          />
          <button
            onClick={() => sendMessage()}
            disabled={loading || !input.trim()}
            className={`px-4 py-2 text-white rounded-xl disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center justify-center ${colors.send}`}
          >
            {loading ? <Loader2 size={18} className="animate-spin" /> : <Send size={18} />}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Main AI Agents Dashboard ─────────────────────────────────────────────────

function AiAgentsDashboard() {
  const [activeAgent, setActiveAgent] = useState('orchestrator');
  const [agentStatus, setAgentStatus] = useState(null);

  const fetchAgentStatus = useCallback(async () => {
    try {
      const data = await adminApi.getAllAgentsStatus();
      setAgentStatus(data);
    } catch {
      setAgentStatus({});
    }
  }, []);

  useEffect(() => {
    fetchAgentStatus();
  }, [fetchAgentStatus]);

  const currentAgent = AGENTS.find(a => a.key === activeAgent);

  const getTabStatus = (agentKey) => {
    const statusKey = AGENTS.find(a => a.key === agentKey)?.statusKey;
    return agentStatus?.[statusKey]?.enabled ?? null;
  };

  return (
    <div className="space-y-4">
      {/* Page header */}
      <div className="flex items-center gap-3 pb-2">
        <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-sm">
          <Bot className="text-white" size={22} />
        </div>
        <div>
          <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            AI Agent Team
            <Sparkles size={16} className="text-purple-500" />
          </h1>
          <p className="text-sm text-gray-500">Đội AI tự động hỗ trợ quản lý E-SHOP</p>
        </div>
      </div>

      {/* Agent tabs */}
      <div className="flex flex-wrap gap-1 border-b border-gray-200">
        {AGENTS.map(agent => {
          const enabled = getTabStatus(agent.key);
          const isActive = activeAgent === agent.key;
          const tabColors = COLOR_MAP[agent.color];
          return (
            <button
              key={agent.key}
              onClick={() => setActiveAgent(agent.key)}
              className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition-colors border-b-2 -mb-px ${
                isActive
                  ? `${tabColors.tab} bg-white`
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
              }`}
            >
              {agent.label}
              {enabled === true && (
                <span className="w-1.5 h-1.5 rounded-full bg-green-400" />
              )}
              {enabled === false && (
                <span className="w-1.5 h-1.5 rounded-full bg-gray-300" />
              )}
            </button>
          );
        })}
      </div>

      {/* Agent chat panel */}
      {currentAgent && (
        <AgentChatPanel
          key={currentAgent.key}
          agent={currentAgent}
          agentStatus={agentStatus}
        />
      )}
    </div>
  );
}

export default AiAgentsDashboard;
