import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../api/api';

const AuthContext = createContext();
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => { checkAuth(); }, []);

  const checkAuth = async () => {
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (token) {
      try {
        // Try to get fresh user data from API
        const userData = await api.getCurrentUser();
        setUser(userData);
        // Update localStorage with fresh data
        localStorage.setItem('user', JSON.stringify(userData));
      } catch (error) {
        console.error('Failed to get user data from API:', error);
        // If API fails but we have saved user data, use it
        if (savedUser) {
          try {
            const parsedUser = JSON.parse(savedUser);
            setUser(parsedUser);
            console.log('Using cached user data:', parsedUser);
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
    return response;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    window.location.href = '/login';
  };

  const hasRole = (role) => {
    console.log('Checking role:', role, 'User roles:', user?.roles);
    if (!user || !user.roles) return false;
    return user.roles.includes(role);
  };

  const isAdmin = () => {
    const result = hasRole('ROLE_ADMIN') || hasRole('ROLE_SUPER_ADMIN');
    console.log('isAdmin result:', result);
    return result;
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, checkAuth, hasRole, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
};
