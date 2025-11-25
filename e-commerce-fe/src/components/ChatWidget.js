import React, { useState, useEffect, useRef, useCallback } from 'react';
import { 
  MessageCircle, 
  X, 
  Send, 
  Paperclip,
  MinusCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

function ChatWidget() {
  const { user } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [conversation, setConversation] = useState(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isInitialized, setIsInitialized] = useState(false);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  // Scroll to bottom when new message
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const markAsRead = useCallback(async (conversationId) => {
    try {
      await api.markMessagesAsRead(conversationId);
      setUnreadCount(0);
    } catch (error) {
      console.error('Error marking as read:', error);
    }
  }, []);

  const fetchNewMessages = useCallback(async () => {
    if (!conversation?.id || loading) return; // Thêm check loading
    
    try {
      const data = await api.getConversationMessages(conversation.id);
      const newMessages = data || [];
      
      // So sánh với số lượng messages hiện tại để tránh re-render không cần thiết
      setMessages(currentMessages => {
        if (newMessages.length > currentMessages.length) {
          if (!isOpen) {
            setUnreadCount(prev => prev + (newMessages.length - currentMessages.length));
          } else {
            markAsRead(conversation.id);
          }
          return newMessages;
        }
        return currentMessages; // Không update nếu không có tin nhắn mới
      });
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  }, [conversation?.id, isOpen, markAsRead, loading]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Load conversation when chat opens - simplified to avoid circular dependency
  useEffect(() => {
    const initializeChat = async () => {
      if (isOpen && user && !conversation && !isInitialized) {
        setIsInitialized(true); // Set flag to prevent multiple calls
        
        try {
          setLoading(true);
          console.log('Loading or creating conversation...');
          
          // Try to get existing conversations first
          const conversations = await api.getUserConversations();
          console.log('Existing conversations:', conversations);
          
          if (conversations && conversations.length > 0) {
            const activeConversation = conversations[0];
            setConversation(activeConversation);
            
            // Load messages directly here
            try {
              const data = await api.getConversationMessages(activeConversation.id);
              setMessages(data || []);
              markAsRead(activeConversation.id);
            } catch (error) {
              console.error('Error loading messages:', error);
            }
            
            console.log('Using existing conversation:', activeConversation);
          } else {
            // Create new conversation
            console.log('Creating new conversation...');
            const newConversation = await api.createConversation({
              subject: 'Hỗ trợ khách hàng'
            });
            console.log('Created new conversation:', newConversation);
            setConversation(newConversation);
            setMessages([]);
          }
        } catch (error) {
          console.error('Error loading conversation:', error);
          setIsInitialized(false); // Reset flag on error
        } finally {
          setLoading(false);
        }
      }
    };

    initializeChat();
  }, [isOpen, user, conversation, isInitialized, markAsRead]);

  // Simulate real-time updates (replace with WebSocket)
  useEffect(() => {
    if (conversation && isOpen && !loading) {
      const interval = setInterval(() => {
        fetchNewMessages();
      }, 10000); // Tăng lên 10 giây để giảm tần suất
      return () => clearInterval(interval);
    }
  }, [conversation, isOpen, fetchNewMessages, loading]);

  const loadOrCreateConversation = useCallback(async () => {
    try {
      setLoading(true);
      console.log('Loading or creating conversation...');
      
      // Try to get existing conversations first
      const conversations = await api.getUserConversations();
      console.log('Existing conversations:', conversations);
      
      if (conversations && conversations.length > 0) {
        const activeConversation = conversations[0];
        setConversation(activeConversation);
        
        // Load messages directly here instead of calling loadMessages
        try {
          const data = await api.getConversationMessages(activeConversation.id);
          setMessages(data || []);
          markAsRead(activeConversation.id);
        } catch (error) {
          console.error('Error loading messages:', error);
        }
        
        console.log('Using existing conversation:', activeConversation);
        return activeConversation;
      } else {
        // Create new conversation
        console.log('Creating new conversation...');
        const newConversation = await api.createConversation({
          subject: 'Hỗ trợ khách hàng'
        });
        console.log('Created new conversation:', newConversation);
        setConversation(newConversation);
        setMessages([]);
        return newConversation;
      }
    } catch (error) {
      console.error('Error loading conversation:', error);
      return null;
    } finally {
      setLoading(false);
    }
  }, [markAsRead]);

  const loadMessages = useCallback(async (conversationId) => {
    try {
      const data = await api.getConversationMessages(conversationId);
      setMessages(data || []);
      
      // Mark messages as read
      markAsRead(conversationId);
    } catch (error) {
      console.error('Error loading messages:', error);
    }
  }, [markAsRead]);

  const sendMessage = async () => {
    console.log('=== sendMessage called ===');
    console.log('newMessage:', newMessage.trim());
    console.log('Current conversation:', conversation);
    
    if (!newMessage.trim()) {
      console.log('No message content, returning');
      return;
    }
    
    try {
      // Ensure we have a conversation
      let currentConversation = conversation;
      if (!currentConversation) {
        console.log('No conversation found, creating one...');
        currentConversation = await loadOrCreateConversation();
        
        if (!currentConversation) {
          console.error('Failed to create conversation');
          alert('Không thể tạo cuộc trò chuyện. Vui lòng thử lại.');
          return;
        }
      }

      console.log('Using conversation:', currentConversation);

      const messageData = {
        conversationId: currentConversation.id,
        content: newMessage.trim(),
        messageType: 'text'
      };

      console.log('Sending message data:', messageData);

      // Optimistic update
      const tempMessage = {
        id: Date.now(),
        content: newMessage.trim(),
        senderType: 'user',
        createdAt: new Date().toISOString(),
        sender: user
      };
      
      setMessages(prev => [...prev, tempMessage]);
      setNewMessage('');

      try {
        console.log('Calling api.sendMessage...');
        const savedMessage = await api.sendMessage(messageData);
        console.log('Message sent successfully:', savedMessage);
        
        // Replace temp message with real one
        setMessages(prev => 
          prev.map(msg => msg.id === tempMessage.id ? savedMessage : msg)
        );
      } catch (apiError) {
        console.error('Error sending message:', apiError);
        // Remove temp message on error
        setMessages(prev => prev.filter(msg => msg.id !== tempMessage.id));
        alert('Không thể gửi tin nhắn. Vui lòng thử lại.');
      }
    } catch (error) {
      console.error('General error in sendMessage:', error);
      alert('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Check file size (10MB max)
    if (file.size > 10 * 1024 * 1024) {
      alert('File quá lớn. Vui lòng chọn file nhỏ hơn 10MB.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('conversationId', conversation.id);

    try {
      setLoading(true);
      const response = await fetch('/api/v1/chat/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      });
      
      const result = await response.json();
      
      // Add file message
      const fileMessage = {
        conversationId: conversation.id,
        content: `Đã gửi file: ${file.name}`,
        messageType: 'file',
        attachmentUrl: result.url,
        attachmentName: file.name
      };
      
      await api.sendMessage(fileMessage);
      
      // Reload messages
      loadMessages(conversation.id);
    } catch (error) {
      console.error('Error uploading file:', error);
      alert('Không thể upload file. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

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
              setUnreadCount(0);
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
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center">
                <MessageCircle size={16} />
              </div>
              <div>
                <h3 className="font-semibold text-sm">Hỗ trợ E-SHOP</h3>
                <p className="text-xs text-red-100">Online - Phản hồi ngay</p>
              </div>
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
                  setIsInitialized(false); // Reset flag when closing
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
                          <div className={`flex ${message.senderType === 'user' ? 'justify-end' : 'justify-start'}`}>
                            <div className={`max-w-xs px-3 py-2 rounded-lg text-sm ${
                              message.senderType === 'user'
                                ? 'bg-red-600 text-white'
                                : 'bg-gray-100 text-gray-800'
                            }`}>
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
                              <div className={`text-xs mt-1 ${
                                message.senderType === 'user' ? 'text-red-100' : 'text-gray-500'
                              }`}>
                                {formatTime(message.createdAt)}
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}
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
                    onChange={(e) => setNewMessage(e.target.value)}
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