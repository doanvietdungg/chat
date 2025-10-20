// Frontend Example - Updated API Service
// File: services/authService.js

class AuthService {
  constructor(baseURL = 'http://localhost:8080') {
    this.baseURL = baseURL;
    this.token = localStorage.getItem('accessToken');
    this.user = JSON.parse(localStorage.getItem('user') || 'null');
  }

  // Set auth data
  setAuthData(authResponse) {
    this.token = authResponse.accessToken;
    this.user = authResponse.user;
    
    localStorage.setItem('accessToken', authResponse.accessToken);
    localStorage.setItem('refreshToken', authResponse.refreshToken);
    localStorage.setItem('user', JSON.stringify(authResponse.user));
  }

  // Clear auth data
  clearAuthData() {
    this.token = null;
    this.user = null;
    
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  // Generic request method
  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    // Add authorization header if token exists
    if (this.token) {
      config.headers.Authorization = `Bearer ${this.token}`;
    }

    try {
      const response = await fetch(url, config);
      const data = await response.json();

      // Check if response follows our standard format
      if (data.success !== undefined) {
        if (data.success) {
          return {
            success: true,
            data: data.data,
            message: data.message,
            timestamp: data.timestamp
          };
        } else {
          throw new Error(data.message || 'Có lỗi xảy ra');
        }
      }

      // Fallback for non-standard responses
      return {
        success: response.ok,
        data: data,
        message: response.ok ? 'Thành công' : 'Có lỗi xảy ra'
      };

    } catch (error) {
      console.error('API Request failed:', error);
      throw new Error(error.message || 'Kết nối thất bại');
    }
  }

  // Auth methods
  async register(userData) {
    try {
      const response = await this.request('/api/v1/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          username: userData.username,
          email: userData.email,
          password: userData.password
        })
      });

      // Set auth data if registration successful
      if (response.data && response.data.accessToken) {
        this.setAuthData(response.data);
      }

      return response;
    } catch (error) {
      throw new Error(error.message || 'Đăng ký thất bại');
    }
  }

  async login(credentials) {
    try {
      const response = await this.request('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({
          usernameOrEmail: credentials.usernameOrEmail,
          password: credentials.password
        })
      });

      // Set auth data if login successful
      if (response.data && response.data.accessToken) {
        this.setAuthData(response.data);
      }

      return response;
    } catch (error) {
      throw new Error(error.message || 'Đăng nhập thất bại');
    }
  }

  async refreshToken() {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('Không có refresh token');
      }

      const response = await this.request('/api/v1/auth/refresh', {
        method: 'POST',
        body: JSON.stringify({
          refreshToken: refreshToken
        })
      });

      // Update tokens (refresh doesn't return user data)
      if (response.data && response.data.accessToken) {
        this.token = response.data.accessToken;
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
      }

      return response;
    } catch (error) {
      this.clearAuthData();
      throw new Error(error.message || 'Làm mới token thất bại');
    }
  }

  // Get current user
  getCurrentUser() {
    return this.user;
  }

  // Check if user is authenticated
  isAuthenticated() {
    return !!this.token && !!this.user;
  }

  // Logout
  logout() {
    this.clearAuthData();
  }
}

// Export singleton instance
export default new AuthService();
