import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../api/api';
import tokenManager from '../utils/tokenManager';
import TokenExpirationModal from '../components/TokenExpirationModal';
import toast from '../utils/toast';

const AuthContext = createContext();
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showExpirationModal, setShowExpirationModal] = useState(false);
  const [timeRemaining, setTimeRemaining] = useState(0);

  const checkAuth = async () => {
    console.log('🔍 checkAuth called');
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    console.log('📦 Token exists:', !!token);
    console.log('👤 Saved user exists:', !!savedUser);
    
    if (token) {
      console.log('checkAuth - Token found, checking expiration...');
      
      // Check if token is expired (but be lenient - add 30s buffer for network delay)
      const isExpired = tokenManager.isTokenExpired(token);
      
      console.log('checkAuth - isExpired:', isExpired);
      
      if (isExpired) {
        console.log('Token expired on page load, clearing auth');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setLoading(false);
        return;
      }

      try {
        // Try to get fresh user data from API
        console.log('checkAuth - Calling getCurrentUser API...');
        const userData = await api.getCurrentUser();
        console.log('checkAuth - Got user data:', userData);
        setUser(userData);
        // Update localStorage with fresh data
        localStorage.setItem('user', JSON.stringify(userData));
        
        // Start monitoring token expiration
        startTokenMonitoring();
      } catch (error) {
        console.error('Failed to get user data from API:', error);
        
        // If 401, token is invalid/expired - silently use cached data if available
        if (error.message.includes('Authentication required')) {
          console.log('checkAuth - Got 401, trying to use cached user data');
          
          // Try to use cached user data first
          if (savedUser) {
            try {
              const parsedUser = JSON.parse(savedUser);
              setUser(parsedUser);
              console.log('✅ Using cached user data:', parsedUser);
              
              // Do NOT start token monitoring here — the token is expired/invalid.
              // The user will need to re-login; DO NOT call startTokenMonitoring()
              // with a missing/expired token as that causes immediate logout loops.
              setLoading(false);
              return;
            } catch (parseError) {
              console.error('Failed to parse saved user data:', parseError);
            }
          }
          
          // If no cached data, clear everything
          console.log('❌ No cached data available, clearing auth');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setUser(null);
          setLoading(false);
          return;
        }
        
        // If API fails but we have saved user data, use it
        if (savedUser) {
          try {
            const parsedUser = JSON.parse(savedUser);
            setUser(parsedUser);
            console.log('Using cached user data:', parsedUser);
            
            // Start monitoring token expiration
            startTokenMonitoring();
          } catch (parseError) {
            console.error('Failed to parse saved user data:', parseError);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
          }
        } else {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
    }
    setLoading(false);
  };

  // Run checkAuth on mount
  useEffect(() => { 
    console.log('🔄 AuthContext mounted - starting checkAuth');
    checkAuth(); 
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const startTokenMonitoring = () => {
    tokenManager.startMonitoring(
      // On expired
      () => {
        console.log('Token expired, forcing logout');
        handleTokenExpired();
      },
      // On expiring soon
      () => {
        console.log('Token expiring soon, showing warning');
        const token = localStorage.getItem('token');
        const remaining = tokenManager.getTimeUntilExpiration(token);
        setTimeRemaining(remaining);
        setShowExpirationModal(true);
      }
    );
  };

  const handleTokenExpired = () => {
    setShowExpirationModal(false);
    logout();
    toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
  };

  const handleDismissModal = () => {
    setShowExpirationModal(false);
    // User chose to continue, they will get 401 on next API call
    console.log('User dismissed expiration warning');
  };

  const login = async (email, password, twoFactorCode = null) => {
    // Validate input
    if (!email || !password) {
      throw new Error('Email và mật khẩu không được để trống');
    }

    const loginData = { 
      usernameOrEmail: email.trim(), // Changed from 'email' to 'usernameOrEmail' to match backend
      password: password 
    };
    
    if (twoFactorCode) {
      loginData.twoFactorCode = twoFactorCode.trim();
    }
    
    console.log('Attempting login with:', { usernameOrEmail: loginData.usernameOrEmail, hasPassword: !!loginData.password, has2FA: !!loginData.twoFactorCode });
    
    const response = await api.login(loginData);
    if (response.require2FA || response.requires2FA) {
      return { requires2FA: true };
    }
    
    localStorage.setItem('token', response.accessToken);
    localStorage.setItem('user', JSON.stringify(response.user));
    setUser(response.user);
    
    // Start monitoring token expiration after login
    startTokenMonitoring();
    
    return response;
  };

  const logout = () => {
    // Stop token monitoring
    tokenManager.stopMonitoring();
    
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setShowExpirationModal(false);
    
    // Redirect to login page, but stay on admin context if coming from admin
    // Since there's only one login page, just redirect to /login for now
    // The login page can handle routing to admin dashboard if user is admin
    window.location.href = '/login';
  };

  const hasRole = (role) => {
    console.log('Checking role:', role, 'User:', user);
    if (!user) return false;
    
    // Handle both role (string) and roles (array)
    if (user.role) {
      // Single role as string
      return user.role === role;
    }
    if (user.roles && Array.isArray(user.roles)) {
      // Multiple roles as array
      return user.roles.includes(role);
    }
    return false;
  };

  const isAdmin = () => {
    const result = hasRole('ADMIN') || hasRole('SUPER_ADMIN');
    console.log('isAdmin check - User:', user, 'Result:', result);
    return result;
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, checkAuth, hasRole, isAdmin }}>
      {children}
      <TokenExpirationModal
        isOpen={showExpirationModal}
        onLogout={logout}
        onDismiss={handleDismissModal}
        timeRemaining={timeRemaining}
      />
    </AuthContext.Provider>
  );
};
