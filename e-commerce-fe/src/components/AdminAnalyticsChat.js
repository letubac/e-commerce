import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Bot, Send, Loader2, MessageSquare, Sparkles, BarChart3 } from 'lucide-react';
import adminApi from '../api/adminApi';
import { toast } from '../utils/toast';

const SUGGESTED_QUESTIONS = [
  'Tổng quan doanh số hôm nay',
  'Thống kê đơn hàng theo trạng thái',
  'Sản phẩm bán chạy nhất',
  'Thống kê người dùng mới',
  'Tình trạng task hiện tại',
];

function generateSessionId() {
  return `session_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

function AdminAnalyticsChat() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [aiStatus, setAiStatus] = useState(null);
  const [statusLoading, setStatusLoading] = useState(true);
  const sessionId = useRef(generateSessionId());
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const fetchAiStatus = useCallback(async () => {
    setStatusLoading(true);
    try {
      const data = await adminApi.getAnalyticsAiStatus();
      setAiStatus(data);
    } catch {
      setAiStatus({ enabled: false });
    } finally {
      setStatusLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAiStatus();
  }, [fetchAiStatus]);

  const sendMessage = async (text) => {
    const messageText = (text || input).trim();
    if (!messageText || loading) return;

    const userMsg = { role: 'user', content: messageText, id: Date.now() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const data = await adminApi.chatAnalytics(messageText, sessionId.current);
      const aiMsg = {
        role: 'ai',
        content: data?.message || data?.response || data || 'Không có phản hồi',
        id: Date.now() + 1,
      };
      setMessages(prev => [...prev, aiMsg]);
    } catch (err) {
      const errMsg = {
        role: 'ai',
        content: `❌ Lỗi: ${err.message || 'Không thể kết nối đến AI Analytics'}`,
        id: Date.now() + 1,
        isError: true,
      };
      setMessages(prev => [...prev, errMsg]);
      toast.error(err.message || 'Lỗi khi gọi AI Analytics');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="flex flex-col h-full min-h-[600px] bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-indigo-50 to-purple-50">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
            <Bot className="text-indigo-600" size={22} />
          </div>
          <div>
            <h2 className="font-bold text-gray-900 flex items-center gap-2">
              AI Analytics
              <Sparkles size={14} className="text-indigo-500" />
            </h2>
            <p className="text-xs text-gray-500">Hỏi đáp thông minh về dữ liệu kinh doanh</p>
          </div>
        </div>
        {statusLoading ? (
          <Loader2 size={16} className="animate-spin text-gray-400" />
        ) : (
          <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${
            aiStatus?.enabled ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
          }`}>
            {aiStatus?.enabled ? '● Đang hoạt động' : '● Không khả dụng'}
          </span>
        )}
      </div>

      {/* AI not enabled warning */}
      {!statusLoading && !aiStatus?.enabled && (
        <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2">
          <BarChart3 size={16} className="text-red-500 shrink-0" />
          <p className="text-sm text-red-700">AI Analytics chưa được kích hoạt. Vui lòng kiểm tra cấu hình.</p>
        </div>
      )}

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full py-12 text-center">
            <MessageSquare size={48} className="text-gray-200 mb-4" />
            <p className="text-gray-500 font-medium mb-1">Bắt đầu cuộc trò chuyện</p>
            <p className="text-xs text-gray-400">Hãy đặt câu hỏi về dữ liệu kinh doanh của bạn</p>
          </div>
        ) : (
          messages.map(msg => (
            <div key={msg.id} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              {msg.role === 'ai' && (
                <div className="w-7 h-7 bg-indigo-100 rounded-full flex items-center justify-center mr-2 mt-0.5 shrink-0">
                  <Bot size={14} className="text-indigo-600" />
                </div>
              )}
              <div className={`max-w-[75%] px-4 py-2.5 rounded-2xl text-sm whitespace-pre-wrap ${
                msg.role === 'user'
                  ? 'bg-red-500 text-white rounded-br-sm'
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
            <div className="w-7 h-7 bg-indigo-100 rounded-full flex items-center justify-center mr-2 mt-0.5 shrink-0">
              <Bot size={14} className="text-indigo-600" />
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
            {SUGGESTED_QUESTIONS.map(q => (
              <button
                key={q}
                onClick={() => sendMessage(q)}
                disabled={loading}
                className="text-xs px-3 py-1.5 bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-full hover:bg-indigo-100 transition-colors disabled:opacity-50"
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
            placeholder="Nhập câu hỏi về dữ liệu kinh doanh... (Enter để gửi)"
            rows={1}
            className="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
            style={{ minHeight: '42px', maxHeight: '120px' }}
            disabled={loading}
          />
          <button
            onClick={() => sendMessage()}
            disabled={loading || !input.trim()}
            className="px-4 py-2 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          >
            {loading ? <Loader2 size={18} className="animate-spin" /> : <Send size={18} />}
          </button>
        </div>
      </div>
    </div>
  );
}

export default AdminAnalyticsChat;
