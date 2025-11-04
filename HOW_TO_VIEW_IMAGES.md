# Hướng Dẫn View Ảnh/File

## URL Format Mới

Backend giờ trả về **full URL** để frontend có thể view trực tiếp.

### Response Example

```json
{
  "success": true,
  "data": {
    "id": "file-uuid",
    "name": "image.jpg",
    "url": "http://localhost:8080/api/v1/files/raw/abc123-1699123456789.jpg",
    "contentType": "image/jpeg",
    "size": 245678
  }
}
```

### Message Response với File

```json
{
  "id": "msg-uuid",
  "text": "Check this out!",
  "type": "IMAGE",
  "file": {
    "id": "file-uuid",
    "name": "image.jpg",
    "url": "http://localhost:8080/api/v1/files/raw/abc123.jpg",
    "contentType": "image/jpeg",
    "size": 245678
  }
}
```

---

## Cách View Ảnh

### 1. Trong HTML

```html
<!-- Direct image tag -->
<img src="http://localhost:8080/api/v1/files/raw/abc123.jpg" alt="Image" />

<!-- From message response -->
<img :src="message.file.url" :alt="message.file.name" />
```

### 2. React Component

```jsx
function MessageImage({ message }) {
  if (!message.file) return null;
  
  return (
    <div className="message-image">
      <img 
        src={message.file.url}
        alt={message.file.name}
        loading="lazy"
        onError={(e) => {
          e.target.src = '/placeholder.png'; // Fallback image
        }}
      />
      {message.text && <p>{message.text}</p>}
    </div>
  );
}
```

### 3. Vue Component

```vue
<template>
  <div class="message-image" v-if="message.file">
    <img 
      :src="message.file.url" 
      :alt="message.file.name"
      loading="lazy"
      @error="handleImageError"
    />
    <p v-if="message.text">{{ message.text }}</p>
  </div>
</template>

<script>
export default {
  props: ['message'],
  methods: {
    handleImageError(e) {
      e.target.src = '/placeholder.png';
    }
  }
}
</script>
```

### 4. Vanilla JavaScript

```javascript
// Get message from API
fetch('/api/v1/chats/chat-id/messages')
  .then(res => res.json())
  .then(data => {
    data.data.content.forEach(message => {
      if (message.file) {
        const img = document.createElement('img');
        img.src = message.file.url; // Full URL ready to use
        img.alt = message.file.name;
        document.getElementById('messages').appendChild(img);
      }
    });
  });
```

---

## Test View Ảnh

### 1. Upload File

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
    "id": "abc-123",
    "url": "http://localhost:8080/api/v1/files/raw/uuid-timestamp.jpg"
  }
}
```

### 2. View Ảnh Trực Tiếp

Mở browser và truy cập URL:
```
http://localhost:8080/api/v1/files/raw/uuid-timestamp.jpg
```

Ảnh sẽ hiển thị trực tiếp trong browser! 🎉

### 3. Send Message và View

```bash
# Send message with file
curl -X POST http://localhost:8080/api/v1/chats/chat-id/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Check this!",
    "type": "IMAGE",
    "fileId": "abc-123"
  }'

# Response includes full URL
{
  "file": {
    "url": "http://localhost:8080/api/v1/files/raw/uuid-timestamp.jpg"
  }
}
```

### 4. List Messages và View Tất Cả Ảnh

```bash
curl http://localhost:8080/api/v1/chats/chat-id/messages \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "msg-1",
        "type": "IMAGE",
        "file": {
          "url": "http://localhost:8080/api/v1/files/raw/image1.jpg"
        }
      },
      {
        "id": "msg-2",
        "type": "IMAGE",
        "file": {
          "url": "http://localhost:8080/api/v1/files/raw/image2.jpg"
        }
      }
    ]
  }
}
```

Tất cả URLs đều ready để view! 🖼️

---

## Configuration

### Development (localhost)

```yaml
# application.yml
spring:
  application:
    base-url: http://localhost:8080
```

### Production

```yaml
# application.yml
spring:
  application:
    base-url: ${APP_BASE_URL:https://api.yourdomain.com}
```

Hoặc set environment variable:
```bash
export APP_BASE_URL=https://api.yourdomain.com
```

---

## CORS Configuration (Nếu Cần)

Nếu frontend chạy trên domain khác (e.g., `http://localhost:3000`), cần config CORS:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

---

## Troubleshooting

### Ảnh không hiển thị

**1. Check URL trong response:**
```bash
curl http://localhost:8080/api/v1/files/file-id | jq '.data.url'

# Should return full URL:
# "http://localhost:8080/api/v1/files/raw/abc123.jpg"
```

**2. Test URL trực tiếp:**
```bash
# Copy URL từ response và mở trong browser
http://localhost:8080/api/v1/files/raw/abc123.jpg
```

**3. Check file tồn tại:**
```bash
# Check uploads folder
ls ./uploads/
# Should see: abc123-1699123456789.jpg
```

**4. Check permissions:**
```bash
# Ensure uploads folder is readable
chmod 755 ./uploads
chmod 644 ./uploads/*
```

### URL không đúng format

**Problem:** URL là `/api/v1/files/raw/...` (relative) thay vì `http://...` (full)

**Solution:** Check config:
```yaml
spring:
  application:
    base-url: http://localhost:8080  # Must be set!
```

### 404 Not Found

**Problem:** URL trả về nhưng file không tồn tại

**Check:**
```sql
-- Check file record in database
SELECT id, name, url FROM files WHERE id = 'file-uuid';

-- Check if file exists on disk
-- Extract filename from URL and check ./uploads/
```

---

## Image Gallery Example

### React Image Gallery

```jsx
function ChatImageGallery({ chatId }) {
  const [images, setImages] = useState([]);
  
  useEffect(() => {
    fetch(`/api/v1/chats/${chatId}/messages`)
      .then(res => res.json())
      .then(data => {
        const imageMessages = data.data.content.filter(
          msg => msg.type === 'IMAGE' && msg.file
        );
        setImages(imageMessages.map(msg => msg.file));
      });
  }, [chatId]);
  
  return (
    <div className="image-gallery">
      {images.map(file => (
        <img 
          key={file.id}
          src={file.url}  // Full URL ready!
          alt={file.name}
          className="gallery-image"
        />
      ))}
    </div>
  );
}
```

### Lightbox/Modal View

```jsx
function ImageModal({ imageUrl, onClose }) {
  return (
    <div className="modal" onClick={onClose}>
      <img 
        src={imageUrl}  // Full URL
        alt="Full size"
        className="modal-image"
      />
    </div>
  );
}
```

---

## Performance Tips

### 1. Lazy Loading

```html
<img src="..." loading="lazy" />
```

### 2. Responsive Images

```html
<img 
  src="http://localhost:8080/api/v1/files/raw/image.jpg"
  srcset="
    http://localhost:8080/api/v1/files/raw/image-small.jpg 400w,
    http://localhost:8080/api/v1/files/raw/image-medium.jpg 800w,
    http://localhost:8080/api/v1/files/raw/image-large.jpg 1200w
  "
  sizes="(max-width: 600px) 400px, (max-width: 1200px) 800px, 1200px"
/>
```

### 3. Caching

```javascript
// Cache file URLs
const fileCache = new Map();

function getCachedUrl(fileId) {
  if (!fileCache.has(fileId)) {
    fetch(`/api/v1/files/${fileId}`)
      .then(res => res.json())
      .then(data => fileCache.set(fileId, data.data.url));
  }
  return fileCache.get(fileId);
}
```

---

## Summary

✅ **Full URL**: Backend trả về `http://localhost:8080/api/v1/files/raw/...`  
✅ **Direct View**: Copy URL vào browser để xem ngay  
✅ **Frontend Ready**: `<img src={message.file.url} />` works immediately  
✅ **No Extra Request**: Không cần gọi API thêm để lấy URL  
✅ **Configurable**: Dễ dàng đổi domain cho production

**Test ngay:**
1. Upload file
2. Copy URL từ response
3. Paste vào browser
4. Ảnh hiển thị! 🎉
