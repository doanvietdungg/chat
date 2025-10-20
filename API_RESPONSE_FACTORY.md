# API Response Factory Documentation

## Tổng quan

Factory này cung cấp một format response chuẩn cho tất cả API endpoints, giúp Frontend dễ dàng xử lý và hiển thị thông báo cho người dùng.

## Format Response Chuẩn

### Success Response
```json
{
  "success": true,
  "message": "Thông báo thành công",
  "data": {
    // Dữ liệu trả về
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "code": 200
}
```

### Error Response
```json
{
  "success": false,
  "message": "Thông báo lỗi",
  "errors": {
    // Chi tiết lỗi (validation errors, etc.)
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "code": 400
}
```

## Cách sử dụng trong Controller

### 1. Import ResponseFactory
```java
import chat.jace.dto.common.ResponseFactory;
```

### 2. Sử dụng các method factory
```java
@RestController
public class ExampleController {
    
    @GetMapping("/example")
    public ResponseEntity<?> getExample() {
        // Success response
        return ResponseFactory.success(data, "Lấy dữ liệu thành công");
    }
    
    @PostMapping("/example")
    public ResponseEntity<?> createExample(@RequestBody RequestDto request) {
        // Created response
        return ResponseFactory.created(data, "Tạo thành công");
    }
    
    @PutMapping("/example/{id}")
    public ResponseEntity<?> updateExample(@PathVariable UUID id, @RequestBody RequestDto request) {
        // Success response
        return ResponseFactory.success(data, "Cập nhật thành công");
    }
    
    @DeleteMapping("/example/{id}")
    public ResponseEntity<?> deleteExample(@PathVariable UUID id) {
        // No content response
        return ResponseFactory.noContent("Xóa thành công");
    }
}
```

## Các method ResponseFactory

### Success Responses
- `ResponseFactory.success(data)` - 200 OK với data
- `ResponseFactory.success(data, message)` - 200 OK với data và message
- `ResponseFactory.created(data)` - 201 Created
- `ResponseFactory.created(data, message)` - 201 Created với message
- `ResponseFactory.noContent()` - 204 No Content
- `ResponseFactory.noContent(message)` - 204 No Content với message

### Error Responses
- `ResponseFactory.badRequest(message)` - 400 Bad Request
- `ResponseFactory.badRequest(message, errors)` - 400 Bad Request với errors
- `ResponseFactory.unauthorized(message)` - 401 Unauthorized
- `ResponseFactory.forbidden(message)` - 403 Forbidden
- `ResponseFactory.notFound(message)` - 404 Not Found
- `ResponseFactory.conflict(message)` - 409 Conflict
- `ResponseFactory.internalError(message)` - 500 Internal Server Error
- `ResponseFactory.validationError(message, fieldErrors)` - 400 Validation Error

### Custom Responses
- `ResponseFactory.custom(status, message)` - Custom HTTP status
- `ResponseFactory.custom(status, message, data)` - Custom HTTP status với data

## Frontend Integration

### JavaScript/TypeScript Service
```javascript
// services/apiService.js
class ApiService {
  async request(endpoint, options = {}) {
    try {
      const response = await fetch(url, config);
      const data = await response.json();

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

      return {
        success: response.ok,
        data: data,
        message: response.ok ? 'Thành công' : 'Có lỗi xảy ra'
      };

    } catch (error) {
      throw new Error(error.message || 'Kết nối thất bại');
    }
  }
}
```

### React Component Example
```jsx
const handleSubmit = async (formData) => {
  try {
    const response = await apiService.register(formData);
    
    if (response.success) {
      // Success - response.data contains the token data
      console.log('Success:', response.message);
      // Handle success (redirect, show message, etc.)
    }
  } catch (error) {
    // Error - error.message contains the error message
    setError(error.message);
  }
};
```

## Exception Handling

GlobalExceptionHandler đã được cập nhật để trả về format chuẩn:

- **Validation Errors**: Trả về 400 với chi tiết lỗi validation
- **Authentication Errors**: Trả về 401 với thông báo xác thực
- **Authorization Errors**: Trả về 403 với thông báo quyền truy cập
- **Business Logic Errors**: Trả về 400 với thông báo lỗi
- **System Errors**: Trả về 500 với thông báo lỗi hệ thống

## Lợi ích

1. **Consistency**: Tất cả API đều trả về cùng một format
2. **Frontend Friendly**: Dễ dàng xử lý và hiển thị thông báo
3. **Error Handling**: Xử lý lỗi một cách nhất quán
4. **Internationalization**: Dễ dàng thêm đa ngôn ngữ
5. **Debugging**: Dễ dàng debug với timestamp và error details

## Ví dụ Response

### Register Success
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  },
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### Validation Error
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "errors": {
    "username": ["Tên đăng nhập không được để trống"],
    "email": ["Email không đúng định dạng", "Email đã tồn tại"],
    "password": ["Mật khẩu phải có ít nhất 8 ký tự"]
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "code": 400
}
```

### Business Logic Error
```json
{
  "success": false,
  "message": "Không có quyền truy cập",
  "timestamp": "2024-01-01T10:00:00Z",
  "code": 403
}
```
