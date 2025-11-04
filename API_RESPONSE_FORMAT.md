# API Response Format

Tất cả các API trong hệ thống đều sử dụng format response chuẩn thông qua `ApiResponse` wrapper.

## Standard Response Structure

### Success Response
```json
{
  "success": true,
  "message": "Thông báo thành công",
  "data": {
    // Response data object
  },
  "code": 200
}
```

### Error Response
```json
{
  "success": false,
  "message": "Thông báo lỗi",
  "code": 400,
  "errors": {
    // Optional error details
  }
}
```

---

## File API Response Examples

### 1. Upload File (POST /files/upload)

**Success (201):**
```json
{
  "success": true,
  "message": "Upload file thành công",
  "data": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "name": "image.jpg",
    "size": 245678,
    "contentType": "image/jpeg",
    "url": "/files/raw/abc123-1699123456789.jpg",
    "uploadedBy": "user-uuid",
    "ownerType": "NONE",
    "ownerId": null,
    "createdAt": "2024-11-04T15:30:00Z"
  },
  "code": 201
}
```

### 2. Get File Info (GET /files/{id})

**Success (200):**
```json
{
  "success": true,
  "message": "Lấy thông tin file thành công",
  "data": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "name": "image.jpg",
    "size": 245678,
    "contentType": "image/jpeg",
    "url": "/files/raw/abc123-1699123456789.jpg",
    "uploadedBy": "user-uuid",
    "ownerType": "MESSAGE",
    "ownerId": "msg-uuid",
    "createdAt": "2024-11-04T15:30:00Z"
  },
  "code": 200
}
```

**Not Found (404):**
```json
{
  "success": false,
  "message": "File không tồn tại",
  "code": 404
}
```

### 3. Delete File (DELETE /files/{id})

**Success (204):**
```json
{
  "success": true,
  "message": "Xóa file thành công",
  "code": 204
}
```

---

## Message API Response Examples

### Send Message (POST /api/v1/chats/{chatId}/messages)

**Success (200):**
```json
{
  "success": true,
  "message": "Gửi tin nhắn thành công",
  "data": {
    "id": "msg-uuid",
    "chatId": "chat-uuid",
    "authorId": "user-uuid",
    "text": "Hello!",
    "type": "TEXT",
    "fileId": null,
    "createdAt": "2024-11-04T15:30:00Z",
    "updatedAt": "2024-11-04T15:30:00Z"
  },
  "code": 200
}
```

**With File:**
```json
{
  "success": true,
  "message": "Gửi tin nhắn thành công",
  "data": {
    "id": "msg-uuid",
    "chatId": "chat-uuid",
    "authorId": "user-uuid",
    "text": "Check this out!",
    "type": "IMAGE",
    "fileId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "createdAt": "2024-11-04T15:30:00Z",
    "updatedAt": "2024-11-04T15:30:00Z"
  },
  "code": 200
}
```

---

## Chat API Response Examples

### List Chats (GET /api/v1/chats)

**Success (200):**
```json
{
  "success": true,
  "message": "Lấy danh sách chat thành công",
  "data": {
    "content": [
      {
        "id": "chat-uuid",
        "name": "Group Chat",
        "type": "GROUP",
        ...
      }
    ],
    "pageable": {...},
    "totalElements": 10,
    "totalPages": 1
  },
  "code": 200
}
```

---

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST (create) requests |
| 204 | No Content | Successful DELETE requests |
| 400 | Bad Request | Validation errors, missing required fields |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | User doesn't have permission |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., duplicate) |
| 500 | Internal Server Error | Server error |

---

## Accessing Data in Frontend

### JavaScript/TypeScript Example

```typescript
// Upload file
const uploadResponse = await fetch('/files/upload', {
  method: 'POST',
  body: formData
});

const result = await uploadResponse.json();

if (result.success) {
  const fileId = result.data.id;
  const fileUrl = result.data.url;
  console.log('File uploaded:', fileId);
} else {
  console.error('Upload failed:', result.message);
}
```

### Axios Example

```typescript
try {
  const response = await axios.post('/files/upload', formData);
  
  // Axios automatically extracts data
  const { success, data, message } = response.data;
  
  if (success) {
    const fileId = data.id;
    // Use fileId to send message
  }
} catch (error) {
  if (error.response) {
    console.error(error.response.data.message);
  }
}
```

### jQuery Example

```javascript
$.ajax({
  url: '/files/upload',
  type: 'POST',
  data: formData,
  processData: false,
  contentType: false,
  success: function(result) {
    if (result.success) {
      var fileId = result.data.id;
      console.log('File ID:', fileId);
    }
  },
  error: function(xhr) {
    var error = xhr.responseJSON;
    alert(error.message);
  }
});
```

---

## Parsing with jq (Command Line)

```bash
# Extract file ID from upload response
FILE_ID=$(curl -s -X POST /files/upload -F "file=@image.jpg" | jq -r '.data.id')

# Check if upload was successful
SUCCESS=$(curl -s -X POST /files/upload -F "file=@image.jpg" | jq -r '.success')

# Get error message
ERROR_MSG=$(curl -s -X GET /files/invalid-id | jq -r '.message')
```

---

## ResponseFactory Methods

Backend sử dụng `ResponseFactory` để tạo response chuẩn:

```java
// Success responses
ResponseFactory.success(data)
ResponseFactory.success(data, "Custom message")
ResponseFactory.created(data, "Created message")
ResponseFactory.noContent("Deleted message")

// Error responses
ResponseFactory.badRequest("Error message")
ResponseFactory.notFound("Not found message")
ResponseFactory.forbidden("Forbidden message")
ResponseFactory.unauthorized("Unauthorized message")
```

---

## Benefits of Standard Format

✅ **Consistency**: All APIs follow the same structure  
✅ **Easy Error Handling**: Frontend can check `success` field  
✅ **Informative**: Clear messages for users  
✅ **Type-Safe**: Easy to create TypeScript interfaces  
✅ **Debugging**: Status codes and messages help identify issues
