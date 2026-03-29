import React, { useState, useEffect, useRef, useCallback, useReducer } from 'react';
import { 
  MessageCircle, 
  X, 
  Send, 
  Paperclip,
  MinusCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

/**
 * ChatWidget - Hỗ trợ khách hàng thời gian thực
 * 
 * Features:
 * - WebSocket connection cho real-time messaging
 * - Message caching & offline support
 * - File upload support
 * - Typing indicators
 * - Read receipts
 * - Responsive design
 * 
 * Log:
 * - 2026-03-07: Upgraded với WebSocket, modern React patterns
 */

// Action types cho useReducer
const ACTIONS = {
  SET_LOADING: 'SET_LOADING',
  SET_MESSAGES: 'SET_MESSAGES',
  ADD_MESSAGE: 'ADD_MESSAGE',
  UPDATE_MESSAGE: 'UPDATE_MESSAGE',
  DELETE_MESSAGE: 'DELETE_MESSAGE',
  RESET_UNREAD: 'RESET_UNREAD',
  INCREMENT_UNREAD: 'INCREMENT_UNREAD',
  SET_CONVERSATION: 'SET_CONVERSATION',
  SET_ERROR: 'SET_ERROR',
  CLEAR_ERROR: 'CLEAR_ERROR'
};

// Reducer function
const chatReducer = (state, action) => {
  switch (action.type) {
    case ACTIONS.SET_LOADING:
      return { ...state, loading: action.payload };
    case ACTIONS.SET_MESSAGES:
      return { ...state, messages: action.payload };
    case ACTIONS.ADD_MESSAGE:
      // Tránh duplicate tin nhắn
      const exists = state.messages.some(m => m.id === action.payload.id);
      return { ...state, messages: exists ? state.messages : [...state.messages, action.payload] };
    case ACTIONS.UPDATE_MESSAGE:
      return {
        ...state,
        messages: state.messages.map(m =>
          m.id === action.payload.id ? { ...m, ...action.payload.updates } : m
        )
      };
    case ACTIONS.DELETE_MESSAGE:
      return { ...state, messages: state.messages.filter(m => m.id !== action.payload) };
    case ACTIONS.RESET_UNREAD:
      return { ...state, unreadCount: 0 };
    case ACTIONS.INCREMENT_UNREAD:
      return { ...state, unreadCount: state.unreadCount + action.payload };
    case ACTIONS.SET_CONVERSATION:
      return { ...state, conversation: action.payload, isInitialized: true };
    case ACTIONS.SET_ERROR:
      return { ...state, error: action.payload };
    case ACTIONS.CLEAR_ERROR:
      return { ...state, error: null };
    default:
      return state;
  }
};

function ChatWidget() {
  const { user } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [newMessage, setNewMessage] = useState('');
  const [wsConnected, setWsConnected] = useState(false);
  const [wsConnecting, setWsConnecting] = useState(false);
  const [typingUsers, setTypingUsers] = useState({});
  const [isAiTyping, setIsAiTyping] = useState(false);
  const websocketRef = useRef(null);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);
  const pollIntervalRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  // Sử dụng useReducer để quản lý complex state
  const [state, dispatch] = useReducer(chatReducer, {
    messages: [],
    loading: false,
    conversation: null,
    unreadCount: 0,
    isInitialized: false,
    error: null
  });

  const { messages, loading, conversation, unreadCount, isInitialized } = state;

  // Scroll to bottom
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  // Kết nối WebSocket cho real-time messaging
  const initializeWebSocket = useCallback(() => {
    if (!conversation || websocketRef.current) return;
    
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      // Connect to backend WebSocket at localhost:8280, not frontend
      const wsUrl = `${wsProtocol}//localhost:8280/ws/chat?token=${token}`;
      
      console.log('[ChatWidget] Kết nối WebSocket tới:', wsUrl);
      setWsConnecting(true);
      
      const ws = new WebSocket(wsUrl);
      
      ws.onopen = () => {
        console.log('✅ [ChatWidget] WebSocket kết nối thành công');
        setWsConnected(true);
        setWsConnecting(false);
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('📨 [ChatWidget] Nhận WebSocket:', data.type, data);

          switch (data.type) {
            case 'CONNECTION_ESTABLISHED':
              console.log('✅ [ChatWidget] Authenticated as:', data.username);
              break;

            case 'NEW_MESSAGE':
              // Nhận tin nhắn mới từ người khác
              if (data.message && data.conversationId === conversation.id) {
                dispatch({
                  type: ACTIONS.ADD_MESSAGE,
                  payload: data.message
                });
              }
              break;

            case 'TYPING':
              // Xử lý typing indicator
              if (data.isTyping) {
                setTypingUsers(prev => ({
                  ...prev,
                  [data.userId]: data.userName
                }));
              } else {
                setTypingUsers(prev => {
                  const updated = { ...prev };
                  delete updated[data.userId];
                  return updated;
                });
              }
              break;

            case 'AI_TYPING':
              // Xử lý AI typing indicator
              setIsAiTyping(data.isTyping === true);
              break;

            case 'MESSAGES_READ':
              // Cập nhật các tin nhắn đã đọc
              dispatch({
                type: ACTIONS.SET_MESSAGES,
                payload: messages.map(m =>
                  m.conversationId === data.conversationId ? { ...m, isRead: true } : m
                )
              });
              break;

            case 'PONG':
              console.log('🔄 [ChatWidget] PONG');
              break;
          }
        } catch (err) {
          console.error('[ChatWidget] Lỗi parse WebSocket message:', err);
        }
      };

      ws.onerror = (error) => {
        console.error('❌ [ChatWidget] WebSocket error:', error);
        setWsConnected(false);
        setWsConnecting(false);
        dispatch({
          type: ACTIONS.SET_ERROR,
          payload: 'Lỗi kết nối WebSocket'
        });
      };

      ws.onclose = () => {
        console.log('❌ [ChatWidget] WebSocket đóng kết nối');
        setWsConnected(false);
        setWsConnecting(false);
        websocketRef.current = null;
      };

      websocketRef.current = ws;
    } catch (error) {
      console.error('[ChatWidget] Lỗi khởi tạo WebSocket:', error);
      setWsConnecting(false);
    }
  }, [conversation, messages]);

  // Cleanup typing timeout
  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
    };
  }, []);

  // Xử lý phím Enter
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // Polling fallback- lấy messages mới mỗi 10 giây
  const fetchNewMessages = useCallback(async () => {
    if (!conversation?.id || loading) return;

    try {
      const response = await api.getConversationMessages(conversation.id, { page: 0, limit: 50 });
      const newMessages = response?.content || response || [];
      
      // So sánh với messages hiện tại
      if (newMessages.length > messages.length) {
        dispatch({ type: ACTIONS.SET_MESSAGES, payload: newMessages });
        
        if (!isOpen) {
          dispatch({
            type: ACTIONS.INCREMENT_UNREAD,
            payload: newMessages.length - messages.length
          });
        } else {
          // Đánh dấu đã đọc ngay
          await api.markMessagesAsRead(conversation.id);
          dispatch({ type: ACTIONS.RESET_UNREAD });
        }
      }
    } catch (error) {
      console.error('[ChatWidget] Lỗi fetch messages:', error);
    }
  }, [conversation?.id, loading, messages.length, isOpen]);

  // Khởi tạo chat conversation
  useEffect(() => {
    const initializeChat = async () => {
      const token = localStorage.getItem('token');
      if (!isOpen || !user || isInitialized || !token) return;

      dispatch({ type: ACTIONS.SET_LOADING, payload: true });

      try {
        // Lấy conversations hiện có
        const response = await api.getUserConversations();
        const conversationsList = response?.content || response || [];
        
        console.log('[ChatWidget] Danh sách conversations:', conversationsList);

        if (conversationsList.length > 0) {
          // Sử dụng conversation đầu tiên
          const activeConv = conversationsList[0];
          dispatch({ type: ACTIONS.SET_CONVERSATION, payload: activeConv });

          // Load messages
          const messagesResponse = await api.getConversationMessages(activeConv.id);
          const messagesList = messagesResponse?.content || messagesResponse || [];
          
          dispatch({ type: ACTIONS.SET_MESSAGES, payload: messagesList });
          // Đánh dấu đã đọc
          await api.markMessagesAsRead(activeConv.id);
          dispatch({ type: ACTIONS.RESET_UNREAD });

          // Khởi tạo WebSocket
          initializeWebSocket();
        } else {
          // Tạo conversation mới với initialMessage
          const newConv = await api.createConversation({
            subject: 'Hỗ trợ khách hàng',
            initialMessage: 'Xin chào, tôi cần hỗ trợ' // 👈 BE yêu cầu @NotBlank
          });
          
          dispatch({ type: ACTIONS.SET_CONVERSATION, payload: newConv });
          dispatch({ type: ACTIONS.SET_MESSAGES, payload: [] });

          // Khởi tạo WebSocket
          initializeWebSocket();
        }

        dispatch({ type: ACTIONS.CLEAR_ERROR });
      } catch (error) {
        console.error('[ChatWidget] Lỗi khởi tạo chat:', error);
        dispatch({
          type: ACTIONS.SET_ERROR,
          payload: error.message || 'Không thể khởi tạo chat'
        });
        
        // Fallback: polling nếu WebSocket không khả dụng
        if (pollIntervalRef.current) clearInterval(pollIntervalRef.current);
        pollIntervalRef.current = setInterval(() => {
          fetchNewMessages();
        }, 10000);
      } finally {
        dispatch({ type: ACTIONS.SET_LOADING, payload: false });
      }
    };

    initializeChat();
  }, [isOpen, user, isInitialized, initializeWebSocket, fetchNewMessages]);

  // Gửi tin nhắn
  const sendMessage = useCallback(async () => {
    if (!newMessage.trim()) return;

    try {
      // Ensure conversation exists
      let currentConv = conversation;
      if (!currentConv) {
        dispatch({ type: ACTIONS.SET_LOADING, payload: true });
        const response = await api.getUserConversations();
        const conversationsList = response?.content || response || [];

        if (conversationsList.length > 0) {
          currentConv = conversationsList[0];
        } else {
          currentConv = await api.createConversation({ subject: 'Hỗ trợ khách hàng' });
        }

        dispatch({ type: ACTIONS.SET_CONVERSATION, payload: currentConv });
      }

      const messageData = {
        conversationId: currentConv.id,
        content: newMessage.trim(),
        messageType: 'TEXT'
      };

      // Optimistic update
      const tempMessage = {
        id: `temp-${Date.now()}`,
        content: newMessage.trim(),
        senderType: 'USER',
        createdAt: new Date().toISOString(),
        isRead: true,
        status: 'SENDING'
      };

      dispatch({ type: ACTIONS.ADD_MESSAGE, payload: tempMessage });
      setNewMessage('');

      // Gửi message via REST API (đảm bảo persistence)
      const savedMessage = await api.sendMessage(messageData);
      
      // Update với real message
      dispatch({
        type: ACTIONS.UPDATE_MESSAGE,
        payload: {
          id: tempMessage.id,
          updates: {
            id: savedMessage.id,
            status: 'SENT',
            ...savedMessage
          }
        }
      });

      // Gửi typing stopped
      if (websocketRef.current?.readyState === WebSocket.OPEN) {
        websocketRef.current.send(JSON.stringify({
          type: 'TYPING',
          conversationId: currentConv.id,
          isTyping: false
        }));
      }

      dispatch({ type: ACTIONS.CLEAR_ERROR });
    } catch (error) {
      console.error('[ChatWidget] Lỗi gửi tin nhắn:', error);
      dispatch({
        type: ACTIONS.SET_ERROR,
        payload: 'Không thể gửi tin nhắn'
      });
      
      // Set timeout để xóa error
      setTimeout(() => dispatch({ type: ACTIONS.CLEAR_ERROR }), 3000);
    }
  }, [newMessage, conversation, dispatch]);

  // Xử lý input change - gửi typing indicator
  const handleInputChange = (e) => {
    setNewMessage(e.target.value);

    // Gửi typing indicator
    if (websocketRef.current?.readyState === WebSocket.OPEN && conversation) {
      websocketRef.current.send(JSON.stringify({
        type: 'TYPING',
        conversationId: conversation.id,
        isTyping: true
      }));

      // Clear existing timeout
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }

      // Gửi typing stopped sau 3 giây inactivity
      typingTimeoutRef.current = setTimeout(() => {
        if (websocketRef.current?.readyState === WebSocket.OPEN) {
          websocketRef.current.send(JSON.stringify({
            type: 'TYPING',
            conversationId: conversation.id,
            isTyping: false
          }));
        }
      }, 3000);
    }
  };

  // Upload file
  const handleFileUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file size
    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    if (file.size > MAX_FILE_SIZE) {
      dispatch({
        type: ACTIONS.SET_ERROR,
        payload: 'File quá lớn (max 10MB)'
      });
      return;
    }

    try {
      dispatch({ type: ACTIONS.SET_LOADING, payload: true });
      const formData = new FormData();
      formData.append('file', file);

      const token = localStorage.getItem('token');
      const response = await fetch('/api/v1/chat/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
      });

      if (!response.ok) {
        throw new Error('Upload failed');
      }

      const result = await response.json();
      
      // Gửi file message
      const fileMessage = {
        conversationId: conversation.id,
        content: `[File: ${file.name}]`,
        messageType: 'FILE',
        attachmentUrl: result.data?.url,
        attachmentName: file.name
      };

      await api.sendMessage(fileMessage);
      
      // Clear input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }

      // Load messages
      await fetchNewMessages();
    } catch (error) {
      console.error('[ChatWidget] Lỗi upload file:', error);
      dispatch({
        type: ACTIONS.SET_ERROR,
        payload: 'Không thể upload file'
      });
    } finally {
      dispatch({ type: ACTIONS.SET_LOADING, payload: false });
    }
  };

  // Format thời gian
  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('vi-VN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return 'Hôm nay';
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Hôm qua';
    } else {
      return date.toLocaleDateString('vi-VN');
    }
  };

  const getMessageBubbleClass = (senderType) => {
    const type = senderType?.toUpperCase();
    if (type === 'USER') return 'bg-red-600 text-white';
    if (type === 'AI') return 'bg-blue-50 text-blue-900 border border-blue-200';
    return 'bg-gray-100 text-gray-800';
  };

  // Render read receipt indicator
  const renderReadReceipt = (message) => {
    if (message.senderType !== 'USER') return null;
    
    return (
      <span className={`text-xs ml-1 ${message.isRead ? 'text-blue-500' : 'text-gray-400'}`}>
        {message.isRead ? '✓✓' : '✓'}
      </span>
    );
  };

  // Render connection status
  const renderConnectionStatus = () => {
    let statusColor = 'bg-gray-400';
    let statusText = 'Chưa kết nối';

    if (wsConnecting) {
      statusColor = 'bg-yellow-400';
      statusText = 'Đang kết nối...';
    } else if (wsConnected) {
      statusColor = 'bg-green-500';
      statusText = 'Đã kết nối';
    }

    return (
      <div className="flex items-center space-x-2">
        <div className={`w-2 h-2 rounded-full ${statusColor} animate-pulse`}></div>
        <span className="text-xs text-gray-600">{statusText}</span>
      </div>
    );
  };

  if (!user) return null;

  return (
    <>
      {/* Chat Button */}
      <div className="fixed bottom-6 right-6 z-50">
        <button
          onClick={() => {
            setIsOpen(true);
            setIsMinimized(false);
            if (unreadCount > 0) {
              dispatch({ type: ACTIONS.RESET_UNREAD });
            }
          }}
          className={`relative w-14 h-14 bg-red-600 hover:bg-red-700 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-300 ${
            isOpen ? 'scale-0' : 'scale-100'
          }`}
        >
          <MessageCircle size={24} />
          {unreadCount > 0 && (
            <span className="absolute -top-2 -right-2 bg-yellow-400 text-red-800 text-xs rounded-full w-6 h-6 flex items-center justify-center font-bold">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </button>
      </div>

      {/* Chat Window */}
      {isOpen && (
        <div className={`fixed bottom-6 right-6 w-80 bg-white rounded-lg shadow-2xl z-50 transition-all duration-300 ${
          isMinimized ? 'h-14' : 'h-96'
        }`}>
          {/* Header */}
          <div className="bg-red-600 text-white p-4 rounded-t-lg flex items-center justify-between">
            <div className="flex items-center space-x-2 flex-1">
              <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center">
                <MessageCircle size={16} />
              </div>
              <div className="flex-1">
                <h3 className="font-semibold text-sm">Hỗ trợ E-SHOP</h3>
                {renderConnectionStatus()}
              </div>
              {conversation?.aiEnabled !== false && (
                <div className="flex items-center gap-1 bg-red-700 px-2 py-0.5 rounded-full text-xs">
                  <span>🤖</span>
                  <span>AI</span>
                </div>
              )}
            </div>
            <div className="flex items-center space-x-2">
              <button
                onClick={() => setIsMinimized(!isMinimized)}
                className="hover:bg-red-700 p-1 rounded"
              >
                <MinusCircle size={16} />
              </button>
              <button
                onClick={() => {
                  setIsOpen(false);
                  dispatch({ type: ACTIONS.SET_CONVERSATION, payload: null });
                }}
                className="hover:bg-red-700 p-1 rounded"
              >
                <X size={16} />
              </button>
            </div>
          </div>

          {!isMinimized && (
            <>
              {/* Messages */}
              <div className="h-64 overflow-y-auto p-4 space-y-3">
                {loading && messages.length === 0 ? (
                  <div className="text-center text-gray-500 text-sm">
                    Đang tải cuộc trò chuyện...
                  </div>
                ) : messages.length === 0 ? (
                  <div className="text-center text-gray-500 text-sm">
                    <div className="mb-2">👋</div>
                    <p>Xin chào! Chúng tôi có thể hỗ trợ gì cho bạn?</p>
                  </div>
                ) : (
                  <>
                    {messages.map((message, index) => {
                      const showDate = index === 0 || 
                        formatDate(message.createdAt) !== formatDate(messages[index - 1].createdAt);
                      
                      return (
                        <div key={message.id}>
                          {showDate && (
                            <div className="text-center text-xs text-gray-400 my-2">
                              {formatDate(message.createdAt)}
                            </div>
                          )}
                          <div className={`flex ${message.senderType?.toUpperCase() === 'USER' ? 'justify-end' : 'justify-start'}`}>
                            <div className={`max-w-xs px-3 py-2 rounded-lg text-sm ${getMessageBubbleClass(message.senderType)}`}>
                              {message.senderType?.toUpperCase() === 'AI' && (
                                <div className="text-xs font-semibold text-blue-500 mb-1 flex items-center gap-1">
                                  <span>🤖</span>
                                  <span>AI Assistant</span>
                                </div>
                              )}
                              {message.messageType === 'file' ? (
                                <div>
                                  <div className="flex items-center space-x-2 mb-1">
                                    <Paperclip size={14} />
                                    <span className="text-xs">File đính kèm</span>
                                  </div>
                                  <a 
                                    href={message.attachmentUrl} 
                                    target="_blank" 
                                    rel="noopener noreferrer"
                                    className="underline hover:no-underline"
                                  >
                                    {message.attachmentName}
                                  </a>
                                </div>
                              ) : (
                                <p className="whitespace-pre-wrap">{message.content}</p>
                              )}
                              <div className={`text-xs mt-1 flex items-center justify-between ${
                                message.senderType?.toUpperCase() === 'USER' ? 'text-red-100' : 'text-gray-500'
                              }`}>
                                <span>{formatTime(message.createdAt)}</span>
                                {renderReadReceipt(message)}
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}

                    {/* Typing indicator */}
                    {Object.keys(typingUsers).length > 0 && (
                      <div className="flex justify-start">
                        <div className="bg-gray-100 text-gray-600 px-3 py-2 rounded-lg text-sm italic">
                          {Object.values(typingUsers).join(', ')} đang nhập...
                        </div>
                      </div>
                    )}

                    {/* AI typing indicator */}
                    {isAiTyping && (
                      <div className="flex justify-start">
                        <div className="bg-blue-50 border border-blue-200 text-blue-700 px-3 py-2 rounded-lg text-sm flex items-center gap-2">
                          <span>🤖</span>
                          <span className="italic">AI Assistant đang trả lời...</span>
                          <span className="flex gap-1">
                            <span className="animate-bounce" style={{ animationDelay: '0ms' }}>●</span>
                            <span className="animate-bounce" style={{ animationDelay: '150ms' }}>●</span>
                            <span className="animate-bounce" style={{ animationDelay: '300ms' }}>●</span>
                          </span>
                        </div>
                      </div>
                    )}

                    <div ref={messagesEndRef} />
                  </>
                )}
              </div>

              {/* Input */}
              <div className="border-t border-gray-200 p-3">
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    className="text-gray-400 hover:text-gray-600 p-1"
                    disabled={loading}
                  >
                    <Paperclip size={18} />
                  </button>
                  <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handleFileUpload}
                    className="hidden"
                    accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx"
                  />
                  <textarea
                    value={newMessage}
                    onChange={handleInputChange}
                    onKeyPress={handleKeyPress}
                    placeholder="Nhập tin nhắn..."
                    className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm resize-none focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    rows="1"
                    disabled={loading}
                  />
                  <button
                    onClick={sendMessage}
                    disabled={!newMessage.trim() || loading}
                    className="bg-red-600 text-white p-2 rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <Send size={16} />
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      )}
    </>
  );
}

export default ChatWidget;