import React, { useState, useEffect, useRef, useCallback } from 'react';
import { 
  MessageCircle, 
  Send, 
  Search, 
  Clock,
  CheckCircle,
  AlertCircle,
  User,
  Archive,
  Star,
  RefreshCw,
  Wifi,
  WifiOff
} from 'lucide-react';
import adminApi from '../api/adminApi';
import adminWebSocket from '../services/adminWebSocketService';

// Backend enum values
const STATUS_LABELS = {
  OPEN: 'Mở',
  ASSIGNED: 'Đang xử lý',
  RESOLVED: 'Đã giải quyết',
  CLOSED: 'Đã đóng',
};

function AdminChatManagement() {
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [sending, setSending] = useState(false);
  const [wsConnected, setWsConnected] = useState(false);
  const [typingUsers, setTypingUsers] = useState({});
  const [handoffAlert, setHandoffAlert] = useState(null); // { conversationId }
  const messagesEndRef = useRef(null);
  const selectedIdRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadConversations = useCallback(async () => {
    try {
      const params = {};
      if (statusFilter !== 'all') params.status = statusFilter;
      const data = await adminApi.getChatConversations(params);
      setConversations(data.content || data || []);
    } catch (error) {
      console.error('Error loading conversations:', error);
    }
  }, [statusFilter]);

  const loadMessages = useCallback(async (conversationId) => {
    try {
      const data = await adminApi.getChatMessages(conversationId, {});
      setMessages(data.content || data || []);
    } catch (error) {
      console.error('Error loading messages:', error);
    }
  }, []);

  // WebSocket lifecycle - khởi tạo kết nối
  useEffect(() => {
    // Kết nối WebSocket
    adminWebSocket.connect();

    // Đăng ký listeners
    adminWebSocket.on('connect', () => {
      console.log('✅ [AdminChat] WebSocket connected');
      setWsConnected(true);
    });

    adminWebSocket.on('disconnect', () => {
      console.log('❌ [AdminChat] WebSocket disconnected');
      setWsConnected(false);
    });

    adminWebSocket.on('newMessage', (message) => {
      console.log('📨 [AdminChat] New message received:', message);
      
      // Cập nhật messages nếu message này thuộc conversation được chọn
      if (selectedIdRef.current === message.conversationId) {
        setMessages(prev => {
          // Tránh duplicate
          const exists = prev.some(m => m.id === message.id);
          return exists ? prev : [...prev, message];
        });
      }

      // Cập nhật conversations list
      setConversations(prev =>
        prev.map(c =>
          c.id === message.conversationId
            ? { ...c, lastMessageAt: message.createdAt, unreadCount: 0 }
            : c
        )
      );
    });

    adminWebSocket.on('messagesRead', ({ conversationId, userId }) => {
      console.log('✓✓ [AdminChat] Messages read for conversation:', conversationId);
      
      // Cập nhật tatus read của các messages
      setMessages(prev =>
        prev.map(m =>
          m.conversationId === conversationId ? { ...m, isRead: true } : m
        )
      );
    });

    adminWebSocket.on('typing', ({ conversationId, userName, isTyping }) => {
      console.log('⌨️ [AdminChat] Typing:', userName, isTyping);
      
      // Chỉ hiển thị nếu đang xem conversation này
      if (selectedIdRef.current === conversationId) {
        setTypingUsers(prev => {
          const updated = { ...prev };
          if (isTyping) {
            updated[userName] = true;
          } else {
            delete updated[userName];
          }
          return updated;
        });
      }
    });

    adminWebSocket.on('error', (error) => {
      console.error('❌ [AdminChat] WebSocket error:', error);
    });

    adminWebSocket.on('humanHandoffRequested', ({ conversationId }) => {
      console.log('🤝 [AdminChat] Human handoff requested for conversation:', conversationId);
      setHandoffAlert({ conversationId });
      // Reload conversations to reflect AI disabled state
      loadConversations();
    });

    // Initial load conversations
    loadConversations();

    // Cleanup
    return () => {
      adminWebSocket.disconnect();
    };
  }, [loadConversations]);

  // Load messages when conversation changes
  useEffect(() => {
    if (!selectedConversation) return;
    
    selectedIdRef.current = selectedConversation.id;
    loadMessages(selectedConversation.id);
    
    // Đánh dấu đã đọc
    adminApi.markConversationAsRead(selectedConversation.id).catch(() => {});

    // Send notification via WebSocket
    if (wsConnected) {
      adminWebSocket.sendRead(selectedConversation.id);
    }
  }, [selectedConversation, loadMessages, wsConnected]);

  // Cleanup
  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
    };
  }, []);

  const sendMessage = async (content = newMessage) => {
    if (!content.trim() || !selectedConversation || sending) return;
    setSending(true);
    try {
      await adminApi.sendChatMessage({
        conversationId: selectedConversation.id,
        content: content.trim(),
        messageType: 'TEXT',
      });
      setNewMessage('');
      await loadMessages(selectedConversation.id);
      loadConversations();

      // Gửi typing stopped notification
      if (wsConnected) {
        adminWebSocket.sendTyping(selectedConversation.id, false);
      }
    } catch (error) {
      console.error('Error sending message:', error);
      alert(error.message || 'Không thể gửi tin nhắn. Vui lòng thử lại.');
    } finally {
      setSending(false);
    }
  };

  // Xử lý input change - gửi typing indicator
  const handleInputChange = (e) => {
    setNewMessage(e.target.value);

    // Gửi typing indicator
    if (wsConnected && selectedConversation) {
      adminWebSocket.sendTyping(selectedConversation.id, true);

      // Clear existing timeout
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }

      // Gửi typing stopped sau 3 giây inactivity
      typingTimeoutRef.current = setTimeout(() => {
        if (wsConnected) {
          adminWebSocket.sendTyping(selectedConversation.id, false);
        }
      }, 3000);
    }
  };

  const updateConversationStatus = async (conversationId, status) => {
    try {
      await adminApi.updateConversationStatus(conversationId, status);
      setConversations(prev =>
        prev.map(c => c.id === conversationId ? { ...c, status } : c)
      );
      if (selectedConversation?.id === conversationId) {
        setSelectedConversation(prev => ({ ...prev, status }));
      }
    } catch (error) {
      console.error('Error updating status:', error);
      alert(error.message || 'Không thể cập nhật trạng thái.');
    }
  };

  const assignToMe = async (conversationId) => {
    try {
      const updated = await adminApi.assignConversation(conversationId);
      setConversations(prev =>
        prev.map(c => c.id === conversationId ? { ...c, ...updated } : c)
      );
      if (selectedConversation?.id === conversationId) {
        setSelectedConversation(prev => ({ ...prev, ...updated }));
      }
    } catch (error) {
      console.error('Error assigning conversation:', error);
      alert(error.message || 'Không thể nhận phụ trách.');
    }
  };

  const toggleAi = async (conversationId, currentAiEnabled) => {
    try {
      const newAiEnabled = !currentAiEnabled;
      const updated = await adminApi.toggleConversationAi(conversationId, newAiEnabled);
      setConversations(prev =>
        prev.map(c => c.id === conversationId ? { ...c, ...updated } : c)
      );
      if (selectedConversation?.id === conversationId) {
        setSelectedConversation(prev => ({ ...prev, ...updated }));
      }
    } catch (error) {
      console.error('Error toggling AI:', error);
      alert(error.message || 'Không thể thay đổi cài đặt AI.');
    }
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    return date.toLocaleDateString('vi-VN');
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'OPEN': return 'bg-blue-100 text-blue-800';
      case 'ASSIGNED': return 'bg-yellow-100 text-yellow-800';
      case 'RESOLVED': return 'bg-green-100 text-green-800';
      case 'CLOSED': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'OPEN': return <AlertCircle size={12} />;
      case 'ASSIGNED': return <Clock size={12} />;
      case 'RESOLVED': return <CheckCircle size={12} />;
      case 'CLOSED': return <Archive size={12} />;
      default: return <MessageCircle size={12} />;
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'URGENT': return 'text-red-600';
      case 'HIGH': return 'text-orange-600';
      case 'NORMAL': return 'text-gray-600';
      case 'LOW': return 'text-green-600';
      default: return 'text-gray-600';
    }
  };

  const filteredConversations = conversations.filter(conv => {
    const matchesSearch = !searchTerm ||
      conv.userName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      conv.subject?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || conv.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="flex h-full bg-gray-50">
      {/* Human Handoff Alert Banner */}
      {handoffAlert && (
        <div role="alert" aria-live="assertive" className="fixed top-4 right-4 z-50 bg-yellow-50 border border-yellow-400 text-yellow-800 px-4 py-3 rounded-lg shadow-lg flex items-center gap-3 max-w-sm">
          <span className="text-xl">🤝</span>
          <div className="flex-1">
            <p className="font-semibold text-sm">Khách hàng cần hỗ trợ!</p>
            <p className="text-xs">Cuộc hội thoại #{handoffAlert.conversationId} đang chờ nhân viên xử lý.</p>
          </div>
          <button
            onClick={() => setHandoffAlert(null)}
            className="text-yellow-600 hover:text-yellow-800 font-bold text-lg leading-none"
          >
            ×
          </button>
        </div>
      )}
      {/* Conversations List */}
      <div className="w-1/3 bg-white border-r border-gray-200 flex flex-col">
        {/* Header */}
        <div className="p-4 border-b border-gray-200">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-semibold">Chat quản lý</h2>
            <div className="flex items-center space-x-2">
              {wsConnected ? (
                <div className="flex items-center space-x-1 text-green-600 text-xs">
                  <Wifi size={14} />
                  <span>Đã kết nối</span>
                </div>
              ) : (
                <div className="flex items-center space-x-1 text-red-600 text-xs">
                  <WifiOff size={14} />
                  <span>Chưa kết nối</span>
                </div>
              )}
            </div>
          </div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-800">Chat Hỗ trợ</h2>
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-600">{conversations.length} cuộc trò chuyện</span>
              <button
                onClick={loadConversations}
                className="text-gray-400 hover:text-gray-600"
                title="Làm mới"
              >
                <RefreshCw size={15} />
              </button>
            </div>
          </div>

          {/* Search */}
          <div className="relative mb-3">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
            <input
              type="text"
              placeholder="Tìm kiếm tên khách hàng, chủ đề..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent text-sm"
            />
          </div>

          {/* Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent text-sm"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="OPEN">Mở</option>
            <option value="ASSIGNED">Đang xử lý</option>
            <option value="RESOLVED">Đã giải quyết</option>
            <option value="CLOSED">Đã đóng</option>
          </select>
        </div>

        {/* Conversations */}
        <div className="flex-1 overflow-y-auto">
          {filteredConversations.length === 0 ? (
            <div className="p-4 text-center text-gray-500">
              <MessageCircle size={48} className="mx-auto mb-2 text-gray-300" />
              <p>Không có cuộc trò chuyện nào</p>
            </div>
          ) : (
            filteredConversations.map((conversation) => (
              <div
                key={conversation.id}
                onClick={() => setSelectedConversation(conversation)}
                className={`p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50 ${
                  selectedConversation?.id === conversation.id ? 'bg-red-50 border-l-4 border-red-500' : ''
                }`}
              >
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                      <User size={16} className="text-gray-600" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h4 className="font-medium text-gray-900 truncate text-sm">
                        {conversation.userName || `Khách hàng #${conversation.userId}`}
                      </h4>
                      {conversation.subject && (
                        <p className="text-xs text-gray-500 truncate">{conversation.subject}</p>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center space-x-1">
                    {conversation.priority && conversation.priority !== 'NORMAL' && (
                      <Star size={12} className={getPriorityColor(conversation.priority)} />
                    )}
                    {conversation.unreadCount > 0 && (
                      <span className="bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {conversation.unreadCount}
                      </span>
                    )}
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs ${getStatusColor(conversation.status)}`}>
                    {getStatusIcon(conversation.status)}
                    {STATUS_LABELS[conversation.status] || conversation.status}
                  </span>
                  <span className="text-xs text-gray-500">
                    {formatTime(conversation.lastMessageAt || conversation.updatedAt)}
                  </span>
                </div>

                {conversation.adminName && (
                  <p className="text-xs text-blue-600 mt-1">
                    Phụ trách: {conversation.adminName}
                  </p>
                )}
              </div>
            ))
          )}
        </div>
      </div>

      {/* Chat Messages */}
      <div className="flex-1 flex flex-col">
        {selectedConversation ? (
          <>
            {/* Chat Header */}
            <div className="bg-white border-b border-gray-200 p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center">
                    <User size={20} className="text-gray-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">
                      {selectedConversation.userName || `Khách hàng #${selectedConversation.userId}`}
                    </h3>
                    {selectedConversation.subject && (
                      <p className="text-sm text-gray-500">{selectedConversation.subject}</p>
                    )}
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  {!selectedConversation.adminId && (
                    <button
                      onClick={() => assignToMe(selectedConversation.id)}
                      className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
                    >
                      Nhận xử lý
                    </button>
                  )}
                  <button
                    onClick={() => toggleAi(selectedConversation.id, selectedConversation.aiEnabled !== false)}
                    className={`px-3 py-1 text-sm rounded flex items-center gap-1 ${
                      selectedConversation.aiEnabled !== false
                        ? 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                        : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                    }`}
                    title={selectedConversation.aiEnabled !== false ? 'Tắt AI tự trả lời' : 'Bật AI tự trả lời'}
                  >
                    <span>🤖</span>
                    <span>{selectedConversation.aiEnabled !== false ? 'AI: Bật' : 'AI: Tắt'}</span>
                  </button>
                  <select
                    value={selectedConversation.status}
                    onChange={(e) => updateConversationStatus(selectedConversation.id, e.target.value)}
                    className="text-sm border border-gray-300 rounded px-3 py-1"
                  >
                    <option value="OPEN">Mở</option>
                    <option value="ASSIGNED">Đang xử lý</option>
                    <option value="RESOLVED">Đã giải quyết</option>
                    <option value="CLOSED">Đã đóng</option>
                  </select>
                </div>
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.length === 0 ? (
                <div className="text-center text-gray-400 mt-10">Chưa có tin nhắn</div>
              ) : (
                messages.map((message) => {
                  const isAdmin = message.senderType === 'ADMIN';
                  const isAi = message.senderType === 'AI';
                  return (
                    <div key={message.id} className={`flex ${isAdmin ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                        isAdmin ? 'bg-red-600 text-white'
                          : isAi ? 'bg-blue-50 text-blue-900 border border-blue-200'
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {isAi && (
                          <div className="text-xs font-semibold text-blue-500 mb-1 flex items-center gap-1">
                            <span>🤖</span>
                            <span>AI Assistant</span>
                          </div>
                        )}
                        <p className="whitespace-pre-wrap">{message.content}</p>
                        <div className={`text-xs mt-1 flex items-center justify-between gap-2 ${isAdmin ? 'text-red-100' : 'text-gray-500'}`}>
                          <span>
                            {new Date(message.createdAt).toLocaleTimeString('vi-VN', {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                            {isAdmin && message.senderName && (
                              <span className="ml-2">- {message.senderName}</span>
                            )}
                          </span>
                          {isAdmin && (
                            <span className={message.isRead ? 'text-blue-300' : 'text-gray-400'}>
                              {message.isRead ? '✓✓' : '✓'}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}

              {/* Typing indicator */}
              {Object.keys(typingUsers).length > 0 && (
                <div className="flex justify-start">
                  <div className="bg-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm italic">
                    {Object.keys(typingUsers).join(', ')} đang nhập...
                  </div>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>

            {/* Message Input */}
            <div className="bg-white border-t border-gray-200 p-4">
              <div className="flex items-end space-x-3">
                <textarea
                  value={newMessage}
                  onChange={handleInputChange}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      sendMessage();
                    }
                  }}
                  placeholder="Nhập tin nhắn... (Enter để gửi, Shift+Enter xuống dòng)"
                  className="flex-1 border border-gray-300 rounded-lg px-4 py-2 resize-none focus:ring-2 focus:ring-red-500 focus:border-transparent text-sm"
                  rows="3"
                />
                <button
                  onClick={() => sendMessage()}
                  disabled={!newMessage.trim() || sending}
                  className="bg-red-600 text-white p-3 rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Send size={20} />
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center bg-gray-50">
            <div className="text-center">
              <MessageCircle size={64} className="mx-auto mb-4 text-gray-300" />
              <h3 className="text-lg font-medium text-gray-600 mb-2">Chọn cuộc trò chuyện</h3>
              <p className="text-gray-500">Chọn một cuộc trò chuyện để bắt đầu hỗ trợ khách hàng</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default AdminChatManagement;