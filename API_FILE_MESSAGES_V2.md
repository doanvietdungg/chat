# API File Messages V2 - Polymorphic File Associations

Tài liệu này mô tả cách gửi file trong chat với kiến trúc polymorphic associations mới.

## Kiến Trúc Mới

### Polymorphic File Associations
File giờ đây có thể thuộc về nhiều loại đối tượng khác nhau:
- **MESSAGE**: File thuộc về message trong chat
- **POST**: File thuộc về post (future feature)
- **PROFILE**: File thuộc về user profile (avatar, cover)
- **CHAT**: File thuộc về chat (group avatar)
- **COMMENT**: File thuộc về comment (future feature)
- **NONE**: File chưa được gắn với đối tượng nào

### Database Schema
```sql
-- FileResource entity
files {
  id UUID PRIMARY KEY,
  name VARCHAR NOT NULL,
  size BIGINT NOT NULL,
  content_type VARCHAR,
  url VARCHAR(1024) NOT NULL,
  uploaded_by UUID,
  owner_type VARCHAR(30),  -- Polymorphic type
  owner_id UUID,           -- Polymorphic ID
  created_at TIMESTAMP
}
```

---

## Flow: Gửi File Trong Chat

### Bước 1: Upload File
Frontend upload file trước, file sẽ có `ownerType = NONE` và `ownerId = null`

```bash
curl -X POST http://localhost:8080/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

**Response:**
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

### Bước 2: Gửi Message với FileId
Frontend gửi message kèm `fileId`, backend sẽ tự động gắn file với message

```bash
curl -X POST http://localhost:8080/api/v1/chats/{chatId}/messages \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Check out this photo!",
    "type": "IMAGE",
    "fileId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
  }'
```

**Response:**
```json
{
  "id": "msg-uuid",
  "chatId": "chat-uuid",
  "authorId": "user-uuid",
  "text": "Check out this photo!",
  "type": "IMAGE",
  "fileId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "file": {
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
  "createdAt": "2024-11-04T15:30:05Z",
  "updatedAt": "2024-11-04T15:30:05Z"
}
```

**Sau khi gửi message, file sẽ được cập nhật:**
```json
{
  "success": true,
  "message": "Lấy thông tin file thành công",
  "data": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "ownerType": "MESSAGE",
    "ownerId": "msg-uuid",
    ...
  },
  "code": 200
}
```

---

## CURL Examples Chi Tiết

### 1. Gửi Ảnh Trong Chat

```bash
# Step 1: Upload ảnh
FILE_RESPONSE=$(curl -s -X POST http://localhost:8080/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@./photo.jpg")

# Extract file ID from ApiResponse wrapper (requires jq)
FILE_ID=$(echo $FILE_RESPONSE | jq -r '.data.id')
echo "Uploaded file ID: $FILE_ID"

# Step 2: Gửi message với ảnh
curl -X POST http://localhost:8080/api/v1/chats/$CHAT_ID/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"text\": \"Look at this!\",
    \"type\": \"IMAGE\",
    \"fileId\": \"$FILE_ID\"
  }"
```

### 2. Gửi File PDF

```bash
# Upload PDF
curl -X POST http://localhost:8080/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@./document.pdf"

# Response: {"success": true, "data": {"id": "file-uuid", ...}}

# Gửi message với PDF
curl -X POST http://localhost:8080/api/v1/chats/chat-uuid/messages \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Here is the report",
    "type": "FILE",
    "fileId": "file-uuid"
  }'
```

### 3. Gửi File Không Có Text

```bash
# Upload file
curl -X POST http://localhost:8080/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@./screenshot.png"

# Gửi message chỉ có file, không có text
curl -X POST http://localhost:8080/api/v1/chats/chat-uuid/messages \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "IMAGE",
    "fileId": "file-uuid"
  }'
```

### 4. Gửi Text Message Thông Thường (Không Có File)

```bash
curl -X POST http://localhost:8080/api/v1/chats/chat-uuid/messages \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Hello everyone!",
    "type": "TEXT"
  }'
```

---

## Message Types

### TEXT
Message chỉ có text, không có file
```json
{
  "text": "Hello",
  "type": "TEXT"
}
```

### IMAGE
Message có ảnh (jpg, png, gif, etc.)
```json
{
  "text": "Optional caption",
  "type": "IMAGE",
  "fileId": "uuid"
}
```

### FILE
Message có file (pdf, doc, zip, etc.)
```json
{
  "text": "Optional description",
  "type": "FILE",
  "fileId": "uuid"
}
```

---

## Validation Rules

### MessageCreateRequest
1. **Phải có ít nhất text hoặc fileId**
   - ✅ `text` only
   - ✅ `fileId` only
   - ✅ `text` + `fileId`
   - ❌ Không có cả hai → Error: "Either text or fileId must be provided"

2. **MessageType**
   - Required field, default = `TEXT`
   - Nên set `IMAGE` khi gửi ảnh
   - Nên set `FILE` khi gửi file khác

3. **ChatId**
   - Required
   - User phải là member của chat

---

## API Endpoints

### 1. Upload File
```
POST /files/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- file: [File]

Response: FileResource
```

### 2. Send Message
```
POST /api/v1/chats/{chatId}/messages
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "text": "Optional text",
  "type": "TEXT|IMAGE|FILE",
  "fileId": "Optional file UUID"
}

Response: MessageResponse
```

### 3. Get File Info
```
GET /files/{fileId}
Authorization: Bearer {token}

Response: FileResource (includes ownerType and ownerId)
```

### 4. Download File
```
GET /files/raw/{storedName}

Response: File binary
```

### 5. Delete File
```
DELETE /files/{fileId}
Authorization: Bearer {token}

Response: {"success": true, "message": "Xóa file thành công", "code": 204}
```

### 6. List Messages
```
GET /api/v1/chats/{chatId}/messages?page=0&size=20
Authorization: Bearer {token}

Response: Page<MessageResponse>
```

---

## Complete Test Scenario

```bash
#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080"
TOKEN="your-jwt-token"
CHAT_ID="your-chat-uuid"

echo "=== Test 1: Send text message ==="
curl -X POST "$BASE_URL/api/v1/chats/$CHAT_ID/messages" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello everyone!", "type": "TEXT"}'

echo -e "\n\n=== Test 2: Upload and send image ==="
# Upload image
IMAGE_RESPONSE=$(curl -s -X POST "$BASE_URL/files/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@./test-image.jpg")
echo "Upload response: $IMAGE_RESPONSE"

IMAGE_ID=$(echo $IMAGE_RESPONSE | jq -r '.id')
echo "Image ID: $IMAGE_ID"

# Send message with image
curl -X POST "$BASE_URL/api/v1/chats/$CHAT_ID/messages" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"text\": \"Check this out!\", \"type\": \"IMAGE\", \"fileId\": \"$IMAGE_ID\"}"

echo -e "\n\n=== Test 3: Upload and send file without text ==="
# Upload PDF
PDF_RESPONSE=$(curl -s -X POST "$BASE_URL/files/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@./document.pdf")
PDF_ID=$(echo $PDF_RESPONSE | jq -r '.id')

# Send message with file only
curl -X POST "$BASE_URL/api/v1/chats/$CHAT_ID/messages" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"type\": \"FILE\", \"fileId\": \"$PDF_ID\"}"

echo -e "\n\n=== Test 4: Get messages ==="
curl -X GET "$BASE_URL/api/v1/chats/$CHAT_ID/messages?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n=== Test 5: Verify file ownership ==="
curl -X GET "$BASE_URL/files/$IMAGE_ID" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Future Extensions

### Sử dụng cho Post (Future)
```bash
# Upload file
curl -X POST /files/upload -F "file=@image.jpg"
# Response: {"id": "file-uuid", "ownerType": "NONE"}

# Create post with file
curl -X POST /api/v1/posts \
  -d '{
    "content": "My post",
    "fileIds": ["file-uuid"]
  }'
# Backend sẽ set ownerType=POST, ownerId=post-uuid
```

### Sử dụng cho Profile Avatar
```bash
# Upload avatar
curl -X POST /files/upload -F "file=@avatar.jpg"

# Update profile
curl -X PUT /api/v1/users/me/avatar \
  -d '{"fileId": "file-uuid"}'
# Backend sẽ set ownerType=PROFILE, ownerId=user-uuid
```

---

## Error Responses

### 400 Bad Request - Missing Content
```json
{
  "success": false,
  "message": "Either text or fileId must be provided",
  "code": 400
}
```

### 403 Forbidden - Not Chat Member
```json
{
  "success": false,
  "message": "Not a chat participant",
  "code": 403
}
```

### 404 Not Found - File Not Found
```json
{
  "success": false,
  "message": "File không tồn tại",
  "code": 404
}
```

### 404 Not Found - Chat Not Found
```json
{
  "success": false,
  "message": "Chat not found",
  "code": 404
}
```

---

## Postman Collection

### Environment Variables
```
baseUrl: http://localhost:8080
token: your-jwt-token
chatId: your-chat-uuid
```

### Request 1: Upload File
```
POST {{baseUrl}}/files/upload
Headers:
  Authorization: Bearer {{token}}
Body (form-data):
  file: [Select File]

Tests:
  pm.test("Status is 201", () => pm.response.to.have.status(201));
  pm.test("Success is true", () => pm.expect(pm.response.json().success).to.be.true);
  pm.environment.set("fileId", pm.response.json().data.id);
```

### Request 2: Send Message with File
```
POST {{baseUrl}}/api/v1/chats/{{chatId}}/messages
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json
Body (raw JSON):
{
  "text": "Check this out!",
  "type": "IMAGE",
  "fileId": "{{fileId}}"
}

Tests:
  pm.test("Status is 200", () => pm.response.to.have.status(200));
  pm.test("Has fileId", () => pm.expect(pm.response.json().fileId).to.exist);
```

### Request 3: Verify File Ownership
```
GET {{baseUrl}}/files/{{fileId}}
Headers:
  Authorization: Bearer {{token}}

Tests:
  pm.test("Success is true", () => pm.expect(pm.response.json().success).to.be.true);
  pm.test("Owner type is MESSAGE", () => {
    pm.expect(pm.response.json().data.ownerType).to.equal("MESSAGE");
  });
```

---

## Lợi Ích Của Kiến Trúc Mới

### 1. Flexibility
- File có thể thuộc về nhiều loại đối tượng
- Dễ dàng mở rộng cho Post, Comment, Profile, etc.

### 2. Separation of Concerns
- Upload file và tạo message là 2 bước độc lập
- Frontend có thể upload file trước, hiển thị progress
- Nếu tạo message fail, file vẫn tồn tại và có thể retry

### 3. Reusability
- Có thể tái sử dụng file cho nhiều mục đích
- Dễ dàng query "tất cả file của user", "tất cả file trong chat", etc.

### 4. Clean Code
- Không cần hardcode relationship giữa file và message
- Service layer rõ ràng, dễ maintain
- Dễ dàng test từng phần riêng biệt

---

## Migration Notes

Nếu đã có database cũ, cần chạy migration:

```sql
-- Add new columns to files table
ALTER TABLE files 
  ADD COLUMN owner_type VARCHAR(30),
  ADD COLUMN owner_id UUID;

-- Set default for existing files
UPDATE files 
SET owner_type = 'NONE' 
WHERE owner_type IS NULL;

-- Create index for better query performance
CREATE INDEX idx_files_owner ON files(owner_type, owner_id);
```
