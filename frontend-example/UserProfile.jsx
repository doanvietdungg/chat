// React Component Example - User Profile
// File: components/UserProfile.jsx

import React, { useState, useEffect } from 'react';
import authService from '../services/authService';

const UserProfile = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Get current user from auth service
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    setLoading(false);
  }, []);

  const handleLogout = () => {
    if (window.confirm('Bạn có chắc chắn muốn đăng xuất?')) {
      authService.logout();
      // Redirect to login page or update UI
      window.location.href = '/login';
    }
  };

  if (loading) {
    return <div className="loading">Đang tải...</div>;
  }

  if (!user) {
    return (
      <div className="not-authenticated">
        <p>Bạn chưa đăng nhập</p>
        <a href="/login">Đăng nhập</a>
      </div>
    );
  }

  return (
    <div className="user-profile">
      <div className="profile-header">
        <div className="avatar">
          {user.avatarUrl ? (
            <img src={user.avatarUrl} alt="Avatar" />
          ) : (
            <div className="avatar-placeholder">
              {user.username.charAt(0).toUpperCase()}
            </div>
          )}
        </div>
        <div className="user-info">
          <h2>{user.username}</h2>
          <p className="email">{user.email}</p>
          {user.emailVerified && (
            <span className="verified-badge">✓ Đã xác thực email</span>
          )}
        </div>
      </div>

      <div className="profile-details">
        <div className="detail-item">
          <label>ID:</label>
          <span>{user.id}</span>
        </div>
        
        <div className="detail-item">
          <label>Tên đăng nhập:</label>
          <span>{user.username}</span>
        </div>
        
        <div className="detail-item">
          <label>Email:</label>
          <span>{user.email}</span>
        </div>
        
        <div className="detail-item">
          <label>Trạng thái email:</label>
          <span className={user.emailVerified ? 'verified' : 'unverified'}>
            {user.emailVerified ? 'Đã xác thực' : 'Chưa xác thực'}
          </span>
        </div>
        
        <div className="detail-item">
          <label>Ngày tạo:</label>
          <span>{new Date(user.createdAt).toLocaleString('vi-VN')}</span>
        </div>
        
        <div className="detail-item">
          <label>Cập nhật lần cuối:</label>
          <span>{new Date(user.updatedAt).toLocaleString('vi-VN')}</span>
        </div>
      </div>

      <div className="profile-actions">
        <button className="edit-profile-btn">
          Chỉnh sửa hồ sơ
        </button>
        <button className="change-password-btn">
          Đổi mật khẩu
        </button>
        <button className="logout-btn" onClick={handleLogout}>
          Đăng xuất
        </button>
      </div>
    </div>
  );
};

export default UserProfile;
