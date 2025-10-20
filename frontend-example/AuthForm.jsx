// React Component Example - Updated AuthForm
// File: components/AuthForm.jsx

import React, { useState } from 'react';
import authService from '../services/authService';

const AuthForm = ({ onAuthSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error when user starts typing
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      let response;
      
      if (isLogin) {
        response = await authService.login({
          usernameOrEmail: formData.username,
          password: formData.password
        });
      } else {
        // Validate password confirmation
        if (formData.password !== formData.confirmPassword) {
          throw new Error('Mật khẩu xác nhận không khớp');
        }

        response = await authService.register({
          username: formData.username,
          email: formData.email,
          password: formData.password
        });
      }

      // Success - response.data contains both token and user info
      console.log('Auth success:', response);
      
      // Extract user info from response
      const userInfo = response.data.user;
      console.log('User info:', userInfo);
      
      // Call success callback with user data
      if (onAuthSuccess) {
        onAuthSuccess({
          user: userInfo,
          tokens: {
            accessToken: response.data.accessToken,
            refreshToken: response.data.refreshToken,
            tokenType: response.data.tokenType
          }
        });
      }

      // Show success message with user info
      const welcomeMessage = isLogin 
        ? `Chào mừng ${userInfo.username}!` 
        : `Chào mừng ${userInfo.username}! Đăng ký thành công!`;
      
      alert(welcomeMessage);

    } catch (err) {
      console.error('Auth error:', err);
      setError(err.message || (isLogin ? 'Đăng nhập thất bại' : 'Đăng ký thất bại'));
    } finally {
      setLoading(false);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setError('');
    setFormData({
      username: '',
      email: '',
      password: '',
      confirmPassword: ''
    });
  };

  return (
    <div className="auth-form">
      <h2>{isLogin ? 'Đăng nhập' : 'Đăng ký'}</h2>
      
      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="username">
            {isLogin ? 'Tên đăng nhập hoặc Email' : 'Tên đăng nhập'}
          </label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleInputChange}
            required
            disabled={loading}
          />
        </div>

        {!isLogin && (
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              required
              disabled={loading}
            />
          </div>
        )}

        <div className="form-group">
          <label htmlFor="password">Mật khẩu</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            required
            disabled={loading}
          />
        </div>

        {!isLogin && (
          <div className="form-group">
            <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              required
              disabled={loading}
            />
          </div>
        )}

        <button 
          type="submit" 
          disabled={loading}
          className="submit-button"
        >
          {loading ? 'Đang xử lý...' : (isLogin ? 'Đăng nhập' : 'Đăng ký')}
        </button>
      </form>

      <div className="toggle-mode">
        <button type="button" onClick={toggleMode} disabled={loading}>
          {isLogin ? 'Chưa có tài khoản? Đăng ký' : 'Đã có tài khoản? Đăng nhập'}
        </button>
      </div>
    </div>
  );
};

export default AuthForm;
