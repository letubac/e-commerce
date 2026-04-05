/**
 * author: LeTuBac
 */
// WebSocket Notification Service
// Uses SockJS + STOMP for real-time notifications

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class NotificationWebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = [];
    this.messageHandlers = [];
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
  }

  /**
   * Connect to WebSocket server
   * @param {string} userId - Current user ID
   * @param {string} userRole - Current user role (ADMIN, USER)
   */
  connect(userId, userRole) {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    const socketUrl = `${process.env.REACT_APP_WS_URL || 'http://localhost:8280'}/ws/notifications`;
    
    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      debug: (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('STOMP Debug:', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('✅ WebSocket connected');
      this.connected = true;
      this.reconnectAttempts = 0;

      // Subscribe to user-specific queue
      if (userId) {
        const userSubscription = this.client.subscribe(
          `/user/${userId}/queue/notifications`,
          this.handleMessage.bind(this)
        );
        this.subscriptions.push(userSubscription);
        console.log(`📬 Subscribed to user queue: ${userId}`);
      }

      // Subscribe to role-based topic
      if (userRole) {
        const roleSubscription = this.client.subscribe(
          `/topic/notifications/${userRole.toLowerCase()}`,
          this.handleMessage.bind(this)
        );
        this.subscriptions.push(roleSubscription);
        console.log(`📢 Subscribed to role topic: ${userRole}`);
      }

      // Subscribe to global announcements
      const globalSubscription = this.client.subscribe(
        '/topic/notifications/all',
        this.handleMessage.bind(this)
      );
      this.subscriptions.push(globalSubscription);
      console.log('🌍 Subscribed to global notifications');
    };

    this.client.onStompError = (frame) => {
      console.error('❌ STOMP Error:', frame.headers['message']);
      console.error('Error details:', frame.body);
    };

    this.client.onWebSocketClose = () => {
      console.log('🔌 WebSocket connection closed');
      this.connected = false;
      this.handleReconnect();
    };

    this.client.activate();
  }

  /**
   * Handle incoming messages
   */
  handleMessage(message) {
    try {
      const notification = JSON.parse(message.body);
      console.log('🔔 New notification received:', notification);

      // Notify all registered handlers
      this.messageHandlers.forEach(handler => {
        try {
          handler(notification);
        } catch (error) {
          console.error('Error in notification handler:', error);
        }
      });
    } catch (error) {
      console.error('Error parsing notification message:', error);
    }
  }

  /**
   * Register a message handler
   * @param {Function} handler - Callback function to handle notifications
   * @returns {Function} Unsubscribe function
   */
  addMessageHandler(handler) {
    this.messageHandlers.push(handler);
    return () => {
      this.messageHandlers = this.messageHandlers.filter(h => h !== handler);
    };
  }

  /**
   * Handle reconnection
   */
  handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`🔄 Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    } else {
      console.error('❌ Max reconnection attempts reached');
    }
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.client && this.connected) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach(subscription => {
        try {
          subscription.unsubscribe();
        } catch (error) {
          console.error('Error unsubscribing:', error);
        }
      });
      this.subscriptions = [];

      // Deactivate client
      this.client.deactivate();
      this.connected = false;
      console.log('👋 WebSocket disconnected');
    }
  }

  /**
   * Check if connected
   */
  isConnected() {
    return this.connected;
  }
}

// Singleton instance
const notificationWebSocketService = new NotificationWebSocketService();

export default notificationWebSocketService;
