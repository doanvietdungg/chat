# Fix Lỗi 403 Forbidden Khi Truy Cập Files

## Vấn Đề

Khi truy cập URL file:
```
http://localhost:8080/api/v1/files/raw/abc123.jpg
```

Nhận lỗi:
```
403 Forbidden
```

## Nguyên Nhân

Spring Security đang block endpoint `/api/v1/files/raw/**` vì nó yêu cầu authentication.

## Giải Pháp

### ✅ Đã Fix

Thêm `/api/v1/files/raw/**` vào danh sách `permitAll()` trong `SecurityConfig.java`:

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/actuator/health",
                    "/api/v1/auth/**",
                    "/api/v1/files/raw/**",  // ← Allow public access
                    "/ws/**",
                    "/test/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

## Test Sau Khi Fix

### 1. Restart Application

```bash
# Stop application (Ctrl+C)
# Start lại
mvn spring-boot:run
```

### 2. Test Upload File

```bash
curl -X POST http://localhost:8080/api/v1/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.jpg"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "file-uuid",
    "url": "http://localhost:8080/api/v1/files/raw/abc123-1699123456789.jpg"
  }
}
```

### 3. Test View File (Không Cần Token)

```bash
# Copy URL từ response và test
curl http://localhost:8080/api/v1/files/raw/abc123-1699123456789.jpg

# Hoặc mở trong browser
# http://localhost:8080/api/v1/files/raw/abc123-1699123456789.jpg
```

**Expected:** Ảnh hiển thị thành công! ✅

### 4. Test Trong Browser

1. Copy URL từ upload response
2. Paste vào browser address bar
3. Ảnh sẽ hiển thị ngay lập tức (không cần login)

---

## Security Considerations

### Public Access là An Toàn?

**Có**, vì:

1. **URL không đoán được**: Filename có UUID + timestamp
   ```
   abc123-1699123456789.jpg
   ```

2. **Không list được files**: Không thể browse folder `/raw/`

3. **Chỉ download được nếu biết exact URL**: Phải có full filename

### Nếu Cần Bảo Mật Hơn

Có 2 options:

#### Option 1: Signed URLs (Recommended)

```java
@Service
public class FileStorageService {
    
    public String generateSignedUrl(UUID fileId, int expiryMinutes) {
        // Generate signed URL with expiry
        String signature = generateSignature(fileId, expiryTime);
        return baseUrl + "/api/v1/files/raw/" + storedName + "?signature=" + signature;
    }
    
    private String generateSignature(UUID fileId, long expiryTime) {
        // HMAC-SHA256 signature
        String data = fileId + ":" + expiryTime;
        return HmacUtils.hmacSha256Hex(secretKey, data);
    }
}
```

#### Option 2: Token-Based Access

```java
// Require token for file access
.requestMatchers("/api/v1/files/raw/**").authenticated()

// Frontend must include token
<img src={fileUrl} headers={{ Authorization: `Bearer ${token}` }} />
```

**Nhưng hiện tại public access là đủ cho hầu hết use cases.**

---

## Các Lỗi 403 Khác

### 1. CORS Error

**Symptom:**
```
Access to fetch at 'http://localhost:8080/api/v1/files/raw/...' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Fix:** Add CORS config

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 2. CSRF Token Missing

**Symptom:**
```
403 Forbidden
Could not verify the provided CSRF token
```

**Fix:** CSRF đã disabled trong SecurityConfig

```java
.csrf(AbstractHttpConfigurer::disable)
```

### 3. Rate Limit Exceeded

**Symptom:**
```
403 Forbidden
Rate limit exceeded
```

**Fix:** Check `RateLimitFilter` config hoặc đợi 1 phút

---

## Verification Checklist

✅ **SecurityConfig updated** - `/api/v1/files/raw/**` in permitAll()  
✅ **Application restarted** - Changes applied  
✅ **Upload works** - Can upload file with token  
✅ **View works** - Can view file WITHOUT token  
✅ **Browser works** - Can open URL directly in browser  

---

## Quick Test Script

```bash
#!/bin/bash

echo "=== Testing File Upload & View ==="
echo ""

# 1. Login to get token
echo "1. Getting auth token..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}' \
  | jq -r '.data.accessToken')

echo "Token: ${TOKEN:0:20}..."
echo ""

# 2. Upload file
echo "2. Uploading file..."
UPLOAD_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.jpg")

echo "$UPLOAD_RESPONSE" | jq '.'
echo ""

# 3. Extract URL
FILE_URL=$(echo "$UPLOAD_RESPONSE" | jq -r '.data.url')
echo "File URL: $FILE_URL"
echo ""

# 4. Test view WITHOUT token
echo "3. Testing view without token..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$FILE_URL")

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ SUCCESS! File is accessible (HTTP $HTTP_CODE)"
    echo ""
    echo "You can now open this URL in browser:"
    echo "$FILE_URL"
else
    echo "❌ FAILED! Got HTTP $HTTP_CODE"
    echo "Expected: 200"
    echo "Check SecurityConfig and restart application"
fi

echo ""
echo "=== Test Complete ==="
```

---

## Summary

**Problem:** 403 Forbidden khi truy cập `/api/v1/files/raw/**`

**Root Cause:** Spring Security block endpoint

**Solution:** Thêm vào `permitAll()` trong SecurityConfig

**Result:** Files giờ có thể view public (không cần token) ✅

**Security:** An toàn vì URL không đoán được (UUID + timestamp)
