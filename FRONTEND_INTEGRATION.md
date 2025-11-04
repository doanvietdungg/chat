# Frontend Integration Guide - File Messages

Hướng dẫn tích hợp frontend để gửi và hiển thị file trong chat.

## Message Response Structure

Khi nhận message có file, response sẽ bao gồm cả `fileId` và object `file` đầy đủ:

```json
{
  "id": "msg-uuid",
  "chatId": "chat-uuid",
  "authorId": "user-uuid",
  "text": "Check out this photo!",
  "type": "IMAGE",
  "fileId": "file-uuid",
  "file": {
    "id": "file-uuid",
    "name": "image.jpg",
    "size": 245678,
    "contentType": "image/jpeg",
    "url": "/files/raw/abc123-1699123456789.jpg",
    "uploadedBy": "user-uuid",
    "ownerType": "MESSAGE",
    "ownerId": "msg-uuid",
    "createdAt": "2024-11-04T15:30:00Z"
  },
  "createdAt": "2024-11-04T15:30:05Z",
  "updatedAt": "2024-11-04T15:30:05Z"
}
```

---

## Flow: Gửi File Trong Chat

### 1. Upload File
```javascript
async function uploadFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch('/files/upload', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  const result = await response.json();
  
  if (result.success) {
    return result.data.id; // Return file ID
  } else {
    throw new Error(result.message);
  }
}
```

### 2. Send Message với File ID
```javascript
async function sendMessage(chatId, text, fileId, messageType) {
  const response = await fetch(`/api/v1/chats/${chatId}/messages`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      text: text,
      type: messageType,
      fileId: fileId
    })
  });
  
  const result = await response.json();
  return result.data; // Returns MessageResponse with file object
}
```

### 3. Complete Flow
```javascript
async function sendFileMessage(chatId, file, caption) {
  try {
    // Step 1: Upload file
    const fileId = await uploadFile(file);
    
    // Step 2: Determine message type
    const messageType = file.type.startsWith('image/') ? 'IMAGE' : 'FILE';
    
    // Step 3: Send message
    const message = await sendMessage(chatId, caption, fileId, messageType);
    
    // Step 4: Display message with file
    displayMessage(message);
    
  } catch (error) {
    console.error('Failed to send file:', error);
    alert('Gửi file thất bại: ' + error.message);
  }
}
```

---

## Hiển Thị Message

### React Component Example

```jsx
function MessageItem({ message }) {
  const renderContent = () => {
    // Text message
    if (message.type === 'TEXT') {
      return <p>{message.text}</p>;
    }
    
    // Image message
    if (message.type === 'IMAGE' && message.file) {
      return (
        <div className="message-image">
          <img 
            src={message.file.url} 
            alt={message.file.name}
            loading="lazy"
          />
          {message.text && <p className="caption">{message.text}</p>}
        </div>
      );
    }
    
    // File message
    if (message.type === 'FILE' && message.file) {
      return (
        <div className="message-file">
          <a 
            href={message.file.url} 
            download={message.file.name}
            className="file-link"
          >
            <FileIcon type={message.file.contentType} />
            <div className="file-info">
              <span className="file-name">{message.file.name}</span>
              <span className="file-size">{formatFileSize(message.file.size)}</span>
            </div>
          </a>
          {message.text && <p className="caption">{message.text}</p>}
        </div>
      );
    }
    
    return null;
  };
  
  return (
    <div className={`message ${message.authorId === currentUserId ? 'sent' : 'received'}`}>
      {renderContent()}
      <span className="timestamp">{formatTime(message.createdAt)}</span>
    </div>
  );
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}
```

### Vue Component Example

```vue
<template>
  <div :class="['message', isOwnMessage ? 'sent' : 'received']">
    <!-- Text message -->
    <p v-if="message.type === 'TEXT'">{{ message.text }}</p>
    
    <!-- Image message -->
    <div v-else-if="message.type === 'IMAGE' && message.file" class="message-image">
      <img :src="message.file.url" :alt="message.file.name" loading="lazy" />
      <p v-if="message.text" class="caption">{{ message.text }}</p>
    </div>
    
    <!-- File message -->
    <div v-else-if="message.type === 'FILE' && message.file" class="message-file">
      <a :href="message.file.url" :download="message.file.name" class="file-link">
        <FileIcon :type="message.file.contentType" />
        <div class="file-info">
          <span class="file-name">{{ message.file.name }}</span>
          <span class="file-size">{{ formatFileSize(message.file.size) }}</span>
        </div>
      </a>
      <p v-if="message.text" class="caption">{{ message.text }}</p>
    </div>
    
    <span class="timestamp">{{ formatTime(message.createdAt) }}</span>
  </div>
</template>

<script>
export default {
  props: ['message', 'currentUserId'],
  computed: {
    isOwnMessage() {
      return this.message.authorId === this.currentUserId;
    }
  },
  methods: {
    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    },
    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString();
    }
  }
}
</script>
```

---

## File Upload với Progress

```javascript
function uploadFileWithProgress(file, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('file', file);
    
    // Track upload progress
    xhr.upload.addEventListener('progress', (e) => {
      if (e.lengthComputable) {
        const percentComplete = (e.loaded / e.total) * 100;
        onProgress(percentComplete);
      }
    });
    
    // Handle completion
    xhr.addEventListener('load', () => {
      if (xhr.status === 201) {
        const result = JSON.parse(xhr.responseText);
        resolve(result.data.id);
      } else {
        reject(new Error('Upload failed'));
      }
    });
    
    // Handle errors
    xhr.addEventListener('error', () => {
      reject(new Error('Network error'));
    });
    
    xhr.open('POST', '/files/upload');
    xhr.setRequestHeader('Authorization', `Bearer ${token}`);
    xhr.send(formData);
  });
}

// Usage
async function sendFileWithProgress(chatId, file, caption) {
  const progressBar = document.getElementById('upload-progress');
  
  try {
    progressBar.style.display = 'block';
    
    const fileId = await uploadFileWithProgress(file, (percent) => {
      progressBar.value = percent;
    });
    
    progressBar.style.display = 'none';
    
    const messageType = file.type.startsWith('image/') ? 'IMAGE' : 'FILE';
    await sendMessage(chatId, caption, fileId, messageType);
    
  } catch (error) {
    progressBar.style.display = 'none';
    alert('Upload failed: ' + error.message);
  }
}
```

---

## Image Preview Before Upload

```javascript
function previewImage(file) {
  return new Promise((resolve, reject) => {
    if (!file.type.startsWith('image/')) {
      reject(new Error('Not an image'));
      return;
    }
    
    const reader = new FileReader();
    
    reader.onload = (e) => {
      resolve(e.target.result); // Base64 data URL
    };
    
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

// Usage in React
function FileUploadPreview({ onSend, onCancel }) {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [caption, setCaption] = useState('');
  
  const handleFileSelect = async (e) => {
    const selectedFile = e.target.files[0];
    if (!selectedFile) return;
    
    setFile(selectedFile);
    
    if (selectedFile.type.startsWith('image/')) {
      const previewUrl = await previewImage(selectedFile);
      setPreview(previewUrl);
    }
  };
  
  const handleSend = async () => {
    if (!file) return;
    await onSend(file, caption);
    setFile(null);
    setPreview(null);
    setCaption('');
  };
  
  return (
    <div className="file-upload-preview">
      <input type="file" onChange={handleFileSelect} />
      
      {preview && (
        <div className="preview">
          <img src={preview} alt="Preview" />
        </div>
      )}
      
      {file && (
        <>
          <input 
            type="text" 
            placeholder="Add a caption..."
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
          />
          <button onClick={handleSend}>Send</button>
          <button onClick={onCancel}>Cancel</button>
        </>
      )}
    </div>
  );
}
```

---

## WebSocket Integration

Khi nhận message qua WebSocket, message cũng sẽ có đầy đủ thông tin file:

```javascript
const stompClient = new StompJs.Client({
  brokerURL: 'ws://localhost:8080/ws',
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  onConnect: () => {
    // Subscribe to chat messages
    stompClient.subscribe(`/topic/chats/${chatId}`, (message) => {
      const messageData = JSON.parse(message.body);
      
      // messageData already contains file object with URL
      displayMessage(messageData);
    });
  }
});

stompClient.activate();
```

---

## TypeScript Interfaces

```typescript
interface FileResponse {
  id: string;
  name: string;
  size: number;
  contentType: string;
  url: string;
  uploadedBy: string;
  ownerType: 'MESSAGE' | 'POST' | 'PROFILE' | 'CHAT' | 'COMMENT' | 'NONE';
  ownerId: string | null;
  createdAt: string;
}

interface MessageResponse {
  id: string;
  chatId: string;
  authorId: string;
  text: string | null;
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
  fileId: string | null;
  file: FileResponse | null;
  createdAt: string;
  updatedAt: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  code: number;
  errors?: any;
}
```

---

## CSS Styling Examples

```css
/* Message container */
.message {
  max-width: 70%;
  margin: 8px 0;
  padding: 8px 12px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message.sent {
  margin-left: auto;
  background-color: #0084ff;
  color: white;
}

.message.received {
  margin-right: auto;
  background-color: #f0f0f0;
  color: black;
}

/* Image message */
.message-image img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  cursor: pointer;
}

.message-image .caption {
  margin-top: 8px;
  font-size: 14px;
}

/* File message */
.message-file {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
}

.file-link:hover {
  background: rgba(0, 0, 0, 0.1);
}

.file-info {
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  font-size: 14px;
}

.file-size {
  font-size: 12px;
  opacity: 0.7;
}

/* Upload progress */
#upload-progress {
  width: 100%;
  height: 4px;
  margin: 8px 0;
}
```

---

## Error Handling

```javascript
async function sendFileMessage(chatId, file, caption) {
  try {
    // Validate file size (e.g., max 10MB)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      throw new Error('File quá lớn. Kích thước tối đa là 10MB');
    }
    
    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
    if (!allowedTypes.includes(file.type)) {
      throw new Error('Loại file không được hỗ trợ');
    }
    
    // Upload and send
    const fileId = await uploadFile(file);
    const messageType = file.type.startsWith('image/') ? 'IMAGE' : 'FILE';
    const message = await sendMessage(chatId, caption, fileId, messageType);
    
    return message;
    
  } catch (error) {
    console.error('Send file error:', error);
    
    // Show user-friendly error
    if (error.message.includes('Network')) {
      alert('Lỗi kết nối. Vui lòng kiểm tra internet');
    } else if (error.message.includes('Unauthorized')) {
      alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại');
    } else {
      alert(error.message);
    }
    
    throw error;
  }
}
```

---

## Optimization Tips

### 1. Lazy Loading Images
```javascript
// Use Intersection Observer for lazy loading
const imageObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      const img = entry.target;
      img.src = img.dataset.src;
      imageObserver.unobserve(img);
    }
  });
});

// Apply to all message images
document.querySelectorAll('.message-image img').forEach(img => {
  imageObserver.observe(img);
});
```

### 2. Image Compression Before Upload
```javascript
async function compressImage(file, maxWidth = 1920, quality = 0.8) {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        
        if (width > maxWidth) {
          height = (height * maxWidth) / width;
          width = maxWidth;
        }
        
        canvas.width = width;
        canvas.height = height;
        
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);
        
        canvas.toBlob((blob) => {
          resolve(new File([blob], file.name, { type: 'image/jpeg' }));
        }, 'image/jpeg', quality);
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  });
}
```

### 3. Caching File URLs
```javascript
const fileCache = new Map();

function getCachedFileUrl(fileId) {
  if (fileCache.has(fileId)) {
    return fileCache.get(fileId);
  }
  return null;
}

function cacheFileUrl(fileId, url) {
  fileCache.set(fileId, url);
}
```

---

## Summary

**Key Points:**
- ✅ Message response bao gồm cả `fileId` và object `file` đầy đủ
- ✅ `file.url` chứa đường dẫn để hiển thị/download file
- ✅ Upload file trước, sau đó gửi message với `fileId`
- ✅ Frontend có thể hiển thị ảnh/file ngay lập tức từ `message.file.url`
- ✅ Hỗ trợ progress tracking khi upload
- ✅ WebSocket message cũng chứa đầy đủ thông tin file
