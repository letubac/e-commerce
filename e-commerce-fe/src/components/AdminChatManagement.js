import React, { useState, useEffect, useRef } from 'react';
import { 
  MessageCircle, 
  Send, 
  Search, 
  Filter,
  MoreVertical,
  Paperclip,
  Clock,
  CheckCircle,
  AlertCircle,
  User,
  Zap,
  Archive,
  Star
} from 'lucide-react';
import adminApi from '../api/adminApi';

function AdminChatManagement() {
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [quickReplies, setQuickReplies] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    loadConversations();
    loadQuickReplies();
    // Set up real-time updates
    const interval = setInterval(loadConversations, 5000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (selectedConversation) {
      loadMessages(selectedConversation.id);
      markAsRead(selectedConversation.id);
    }
  }, [selectedConversation]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadConversations = async () => {
    try {
      const params = {};
      if (searchTerm) params.search = searchTerm;
      if (statusFilter !== 'all') params.status = statusFilter;
      
      const data = await adminApi.getChatConversations(params);
      setConversations(data.content || data || []);
    } catch (error) {
      console.error('Error loading conversations:', error);
      // Display error message from BE if available
      if (error.message) {
        alert(`Lỗi: ${error.message}`);
      }
    }
  };

  const loadMessages = async (conversationId) => {
    try {
      const data = await adminApi.getChatMessages(conversationId, {});
      setMessages(data.content || data || []);
    } catch (error) {
      console.error('Error loading messages:', error);
      if (error.message) {
        alert(`Lỗi: ${error.message}`);
      }
    }
  };

  const loadQuickReplies = async () => {
    try {
      const data = await adminApi.getChatQuickReplies();
      setQuickReplies(data || []);
    } catch (error) {
      console.error('Error loading quick replies:', error);
    }
  };

  const sendMessage = async (content = newMessage) => {
    if (!content.trim() || !selectedConversation) return;

    const messageData = {
      conversationId: selectedConversation.id,
      content: content.trim(),
      messageType: 'text'
    };

    try {
      await adminApi.sendChatMessage(messageData);
      setNewMessage('');
      loadMessages(selectedConversation.id);
      loadConversations(); // Update conversation list
    } catch (error) {
      console.error('Error sending message:', error);
      alert(error.message || 'Không thể gửi tin nhắn. Vui lòng thử lại.');
    }
  };

  const markAsRead = async (conversationId) => {
    try {
      await adminApi.markConversationAsRead(conversationId);
    } catch (error) {
      console.error('Error marking as read:', error);
    }
  };

  const updateConversationStatus = async (conversationId, status) => {
    try {
      await adminApi.updateConversationStatus(conversationId, status);
      loadConversations();
      if (selectedConversation?.id === conversationId) {
        setSelectedConversation(prev => ({ ...prev, status }));
      }
    } catch (error) {
      console.error('Error updating status:', error);
      if (error.message) {
        alert(`Lỗi: ${error.message}`);
      }
    }
  };

  const assignToMe = async (conversationId) => {
    try {
      await adminApi.assignConversation(conversationId);
      loadConversations();
    } catch (error) {
      console.error('Error assigning conversation:', error);
      if (error.message) {
        alert(`Lỗi: ${error.message}`);
      }
    }
  };

  const formatTime = (timestamp) => {
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
      case 'open': return 'bg-blue-100 text-blue-800';
      case 'in_progress': return 'bg-yellow-100 text-yellow-800';
      case 'resolved': return 'bg-green-100 text-green-800';
      case 'closed': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'open': return <AlertCircle size={12} />;
      case 'in_progress': return <Clock size={12} />;
      case 'resolved': return <CheckCircle size={12} />;
      case 'closed': return <Archive size={12} />;
      default: return <MessageCircle size={12} />;
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'urgent': return 'text-red-600';
      case 'high': return 'text-orange-600';
      case 'normal': return 'text-gray-600';
      case 'low': return 'text-green-600';
      default: return 'text-gray-600';
    }
  };

  const filteredConversations = conversations.filter(conv => {
    const matchesSearch = !searchTerm || 
      conv.user_name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      conv.user_email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      conv.last_message?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || conv.status === statusFilter;
    
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="flex h-full bg-gray-50">
      {/* Conversations List */}
      <div className="w-1/3 bg-white border-r border-gray-200 flex flex-col">
        {/* Header */}
        <div className="p-4 border-b border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-800">Chat Hỗ trợ</h2>
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-600">{conversations.length} cuộc trò chuyện</span>
            </div>
          </div>
          
          {/* Search */}
          <div className="relative mb-3">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
            <input
              type="text"
              placeholder="Tìm kiếm cuộc trò chuyện..."
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
            <option value="open">Mở</option>
            <option value="in_progress">Đang xử lý</option>
            <option value="resolved">Đã giải quyết</option>
            <option value="closed">Đã đóng</option>
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
                        {conversation.user_name || conversation.user_email}
                      </h4>
                      <p className="text-xs text-gray-500">{conversation.user_email}</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-1">
                    {conversation.priority && conversation.priority !== 'normal' && (
                      <Star size={12} className={getPriorityColor(conversation.priority)} />
                    )}
                    {conversation.unread_count > 0 && (
                      <span className="bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {conversation.unread_count}
                      </span>
                    )}
                  </div>
                </div>
                
                <div className="flex items-center justify-between mb-2">
                  <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs ${getStatusColor(conversation.status)}`}>
                    {getStatusIcon(conversation.status)}
                    {conversation.status === 'open' ? 'Mở' :
                     conversation.status === 'in_progress' ? 'Đang xử lý' :
                     conversation.status === 'resolved' ? 'Đã giải quyết' : 'Đã đóng'}
                  </span>
                  <span className="text-xs text-gray-500">
                    {formatTime(conversation.last_message_at || conversation.updated_at)}
                  </span>
                </div>

                {conversation.last_message && (
                  <p className="text-sm text-gray-600 truncate">
                    {conversation.last_message}
                  </p>
                )}

                {conversation.admin_name && (
                  <p className="text-xs text-blue-600 mt-1">
                    Được phụ trách bởi: {conversation.admin_name}
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
                      {selectedConversation.user_name || selectedConversation.user_email}
                    </h3>
                    <p className="text-sm text-gray-500">{selectedConversation.user_email}</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  {/* Status dropdown */}
                  <select
                    value={selectedConversation.status}
                    onChange={(e) => updateConversationStatus(selectedConversation.id, e.target.value)}
                    className="text-sm border border-gray-300 rounded px-3 py-1"
                  >
                    <option value="open">Mở</option>
                    <option value="in_progress">Đang xử lý</option>
                    <option value="resolved">Đã giải quyết</option>
                    <option value="closed">Đã đóng</option>
                  </select>

                  {!selectedConversation.admin_id && (
                    <button
                      onClick={() => assignToMe(selectedConversation.id)}
                      className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
                    >
                      Nhận xử lý
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${message.sender_type === 'admin' ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                    message.sender_type === 'admin'
                      ? 'bg-red-600 text-white'
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    {message.message_type === 'file' ? (
                      <div>
                        <div className="flex items-center space-x-2 mb-1">
                          <Paperclip size={14} />
                          <span className="text-xs">File đính kèm</span>
                        </div>
                        <a 
                          href={message.attachment_url} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="underline hover:no-underline"
                        >
                          {message.attachment_name}
                        </a>
                      </div>
                    ) : (
                      <p className="whitespace-pre-wrap">{message.content}</p>
                    )}
                    <div className={`text-xs mt-1 ${
                      message.sender_type === 'admin' ? 'text-red-100' : 'text-gray-500'
                    }`}>
                      {new Date(message.created_at).toLocaleTimeString('vi-VN', { 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      })}
                      {message.sender_type === 'admin' && (
                        <span className="ml-2">- {message.sender?.full_name || 'Admin'}</span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>

            {/* Quick Replies */}
            {quickReplies.length > 0 && (
              <div className="bg-gray-50 border-t border-gray-200 p-3">
                <div className="flex flex-wrap gap-2">
                  <span className="text-xs text-gray-600 font-medium">Tin nhắn mẫu:</span>
                  {quickReplies.slice(0, 4).map((reply) => (
                    <button
                      key={reply.id}
                      onClick={() => sendMessage(reply.content)}
                      className="px-3 py-1 bg-white border border-gray-300 rounded-full text-xs hover:bg-gray-100"
                    >
                      {reply.title}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Message Input */}
            <div className="bg-white border-t border-gray-200 p-4">
              <div className="flex items-end space-x-3">
                <button className="text-gray-400 hover:text-gray-600 p-2">
                  <Paperclip size={20} />
                </button>
                <textarea
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      sendMessage();
                    }
                  }}
                  placeholder="Nhập tin nhắn..."
                  className="flex-1 border border-gray-300 rounded-lg px-4 py-2 resize-none focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  rows="3"
                />
                <button
                  onClick={() => sendMessage()}
                  disabled={!newMessage.trim()}
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