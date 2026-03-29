/**
 * Admin WebSocket Service - Real-time chat updates
 * 
 * Replaces polling with WebSocket for instant updates
 * - Conversation list updates (new conversations)
 * - Message updates (new messages, read receipts)
 * - Typing indicators
 * - Connection status
 */

class AdminWebSocketService {
  constructor() {
    this.ws = null;
    this.isConnected = false;
    this.isConnecting = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 5000;
    this.listeners = {
      onConnect: null,
      onDisconnect: null,
      onNewMessage: null,
      onMessagesRead: null,
      onTyping: null,
      onNewConversation: null,
      onError: null
    };
  }

  /**
   * Connect to WebSocket
   */
  connect() {
    if (this.isConnected || this.isConnecting) return;

    this.isConnecting = true;
    const token = localStorage.getItem('token');

    if (!token) {
      console.warn('[AdminWebSocket] No token found');
      this.isConnecting = false;
      return;
    }

    try {
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      // Connect to backend WebSocket at localhost:8280, not frontend
      const wsUrl = `${wsProtocol}//localhost:8280/ws/chat?token=${token}`;

      console.log('[AdminWebSocket] Connecting to:', wsUrl);

      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        console.log('✅ [AdminWebSocket] Connected');
        this.isConnected = true;
        this.isConnecting = false;
        this.reconnectAttempts = 0;

        if (this.listeners.onConnect) {
          this.listeners.onConnect();
        }
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.handleMessage(data);
        } catch (err) {
          console.error('[AdminWebSocket] Error parsing message:', err);
        }
      };

      this.ws.onerror = (error) => {
        console.error('❌ [AdminWebSocket] Error:', error);
        this.isConnected = false;
        this.isConnecting = false;

        if (this.listeners.onError) {
          this.listeners.onError(error);
        }
      };

      this.ws.onclose = () => {
        console.log('❌ [AdminWebSocket] Disconnected');
        this.isConnected = false;
        this.isConnecting = false;

        if (this.listeners.onDisconnect) {
          this.listeners.onDisconnect();
        }

        // Auto reconnect
        this.attemptReconnect();
      };
    } catch (error) {
      console.error('[AdminWebSocket] Connection error:', error);
      this.isConnecting = false;

      if (this.listeners.onError) {
        this.listeners.onError(error);
      }
    }
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.isConnected = false;
    this.isConnecting = false;
  }

  /**
   * Attempt reconnection with exponential backoff
   */
  attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn('[AdminWebSocket] Max reconnect attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

    console.log(`[AdminWebSocket] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      this.connect();
    }, delay);
  }

  /**
   * Handle incoming WebSocket messages
   */
  handleMessage(data) {
    console.log('📨 [AdminWebSocket] Received:', data.type);

    switch (data.type) {
      case 'CONNECTION_ESTABLISHED':
        console.log('✅ [AdminWebSocket] Authenticated as:', data.username);
        break;

      case 'NEW_MESSAGE':
        // Broadcast new message to all listeners
        if (this.listeners.onNewMessage) {
          this.listeners.onNewMessage(data.message);
        }
        break;

      case 'MESSAGES_READ':
        // Someone marked messages as read
        if (this.listeners.onMessagesRead) {
          this.listeners.onMessagesRead({
            conversationId: data.conversationId,
            userId: data.userId
          });
        }
        break;

      case 'TYPING':
        // Someone is typing
        if (this.listeners.onTyping) {
          this.listeners.onTyping({
            conversationId: data.conversationId,
            userId: data.userId,
            userName: data.userName,
            isTyping: data.isTyping
          });
        }
        break;

      case 'PONG':
        // Keep-alive response
        break;

      default:
        console.warn('[AdminWebSocket] Unknown message type:', data.type);
    }
  }

  /**
   * Send typing indicator
   */
  sendTyping(conversationId, isTyping) {
    if (!this.isConnected) {
      console.warn('[AdminWebSocket] Not connected, cannot send typing indicator');
      return;
    }

    this.ws.send(JSON.stringify({
      type: 'TYPING',
      conversationId,
      isTyping
    }));
  }

  /**
   * Send read notification
   */
  sendRead(conversationId) {
    if (!this.isConnected) {
      console.warn('[AdminWebSocket] Not connected, cannot send read notification');
      return;
    }

    this.ws.send(JSON.stringify({
      type: 'READ',
      conversationId
    }));
  }

  /**
   * Send keep-alive ping
   */
  sendPing() {
    if (!this.isConnected) return;

    this.ws.send(JSON.stringify({
      type: 'PING'
    }));
  }

  /**
   * Register listener for specific event
   */
  on(event, callback) {
    if (this.listeners.hasOwnProperty(`on${event.charAt(0).toUpperCase()}${event.slice(1)}`)) {
      this.listeners[`on${event.charAt(0).toUpperCase()}${event.slice(1)}`] = callback;
    }
  }

  /**
   * Remove listener
   */
  off(event) {
    if (this.listeners.hasOwnProperty(`on${event.charAt(0).toUpperCase()}${event.slice(1)}`)) {
      this.listeners[`on${event.charAt(0).toUpperCase()}${event.slice(1)}`] = null;
    }
  }

  /**
   * Get connection status
   */
  getStatus() {
    return {
      connected: this.isConnected,
      connecting: this.isConnecting,
      readyState: this.ws?.readyState ?? -1
    };
  }
}

// Singleton instance
const adminWebSocket = new AdminWebSocketService();

export default adminWebSocket;
