# Auth Response với User Information

## Tổng quan

Đã cập nhật AuthController để trả về thông tin user cùng với token trong response của login và register.

## Format Response Mới

### Register/Login Success Response
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "john_doe",
      "email": "john@example.com",
      "avatarUrl": "https://example.com/avatar.jpg",
      "emailVerified": true,
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  },
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### Refresh Token Response (không có user info)
```json
{
  "success": true,
  "message": "Làm mới token thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  },
  "timestamp": "2024-01-01T10:00:00Z"
}
```

## Các file đã tạo/cập nhật

### 1. UserResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private boolean emailVerified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

### 2. AuthResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserResponse user;
}
```

### 3. AuthService.java - Cập nhật
- `register()` method trả về `AuthResponse` thay vì `TokenResponse`
- `login()` method trả về `AuthResponse` thay vì `TokenResponse`
- `refresh()` method vẫn trả về `TokenResponse` (không cần user info)
- Thêm method `toUserResponse()` để convert User entity sang UserResponse

### 4. AuthController.java - Cập nhật
- Import `AuthResponse`
- Cập nhật `register()` và `login()` methods để sử dụng `AuthResponse`
- `refresh()` method vẫn sử dụng `TokenResponse`

## Frontend Integration

### JavaScript Service Example
```javascript
class AuthService {
  async login(credentials) {
    const response = await this.request('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });

    if (response.data && response.data.accessToken) {
      // Lưu cả token và user info
      this.setAuthData(response.data);
    }

    return response;
  }

  setAuthData(authResponse) {
    this.token = authResponse.accessToken;
    this.user = authResponse.user;
    
    localStorage.setItem('accessToken', authResponse.accessToken);
    localStorage.setItem('refreshToken', authResponse.refreshToken);
    localStorage.setItem('user', JSON.stringify(authResponse.user));
  }

  getCurrentUser() {
    return this.user;
  }
}
```

### React Component Example
```jsx
const AuthForm = ({ onAuthSuccess }) => {
  const handleSubmit = async (formData) => {
    try {
      const response = await authService.login(formData);
      
      if (response.success) {
        const userInfo = response.data.user;
        const tokens = {
          accessToken: response.data.accessToken,
          refreshToken: response.data.refreshToken,
          tokenType: response.data.tokenType
        };
        
        // Call success callback with both user and tokens
        onAuthSuccess({ user: userInfo, tokens });
        
        // Show welcome message with user name
        alert(`Chào mừng ${userInfo.username}!`);
      }
    } catch (error) {
      setError(error.message);
    }
  };
};
```

## Lợi ích

1. **Immediate User Info**: Frontend có ngay thông tin user sau khi login/register
2. **Reduced API Calls**: Không cần gọi thêm API để lấy thông tin user
3. **Better UX**: Có thể hiển thị tên user ngay lập tức
4. **Consistent Data**: User info được đồng bộ với token
5. **Caching**: Frontend có thể cache user info trong localStorage

## Ví dụ sử dụng

### 1. Login và hiển thị user info
```javascript
const response = await authService.login({
  usernameOrEmail: 'john@example.com',
  password: 'password123'
});

if (response.success) {
  const user = response.data.user;
  console.log(`Welcome ${user.username}!`);
  console.log(`Email: ${user.email}`);
  console.log(`Verified: ${user.emailVerified}`);
}
```

### 2. Lưu user info vào state
```jsx
const [user, setUser] = useState(null);

useEffect(() => {
  const currentUser = authService.getCurrentUser();
  setUser(currentUser);
}, []);

return (
  <div>
    {user && (
      <div>
        <h2>Welcome, {user.username}!</h2>
        <p>Email: {user.email}</p>
        {user.emailVerified && <span>✓ Verified</span>}
      </div>
    )}
  </div>
);
```

### 3. Check authentication status
```javascript
if (authService.isAuthenticated()) {
  const user = authService.getCurrentUser();
  console.log(`User ${user.username} is logged in`);
} else {
  console.log('User is not authenticated');
}
```

## Migration Notes

- **Backward Compatibility**: Refresh token endpoint vẫn trả về format cũ
- **Frontend Update**: Cần cập nhật frontend để handle user info trong response
- **Token Storage**: Có thể lưu user info cùng với token để tránh gọi API
- **Error Handling**: User info chỉ có trong login/register success response
