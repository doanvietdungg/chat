# API Curl Examples

## Authentication

### 1. Login để lấy token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "123456"
  }'
```

### 2. Login với test user
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "test1@chat.com",
    "password": "123456"
  }'
```

**Response sẽ trả về:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440011",
      "username": "admin",
      "email": "admin@chat.com"
    }
  }
}
```

**Lưu token để dùng cho các API khác:**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

---

## User Search APIs

### 1. Tìm kiếm users
```bash
curl -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "alice",
    "limit": 10
  }'
```

### 2. Tìm kiếm theo email
```bash
curl -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "bob@example.com",
    "limit": 5
  }'
```

### 3. Lấy lịch sử tìm kiếm gần đây
```bash
curl -X GET http://localhost:8080/api/v1/users/search/recent \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Lấy gợi ý liên hệ
```bash
curl -X GET http://localhost:8080/api/v1/users/search/suggested \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Lưu lịch sử tìm kiếm
```bash
curl -X POST http://localhost:8080/api/v1/users/search/history/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Xóa lịch sử tìm kiếm
```bash
curl -X DELETE http://localhost:8080/api/v1/users/search/history \
  -H "Authorization: Bearer $TOKEN"
```

---

## Contact Management APIs

### 1. Thêm liên hệ
```bash
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contactUserId": "550e8400-e29b-41d4-a716-446655440002",
    "displayName": "Bob Smith"
  }'
```

### 2. Thêm liên hệ không có display name
```bash
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contactUserId": "550e8400-e29b-41d4-a716-446655440003"
  }'
```

### 3. Lấy danh sách liên hệ (có phân trang)
```bash
curl -X GET "http://localhost:8080/api/v1/contacts?page=0&size=10&sort=updatedAt,desc" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Lấy thông tin một liên hệ cụ thể
```bash
curl -X GET http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Cập nhật display name của liên hệ
```bash
curl -X PUT http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Bobby Smith (Updated)"
  }'
```

### 6. Xóa liên hệ
```bash
curl -X DELETE http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Existing APIs (for reference)

### Chat APIs
```bash
# Lấy danh sách chats
curl -X GET "http://localhost:8080/api/v1/chats?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Tạo group chat mới
curl -X POST http://localhost:8080/api/v1/chats \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "GROUP",
    "title": "New Test Group",
    "description": "Testing group creation"
  }'
```

### Message APIs
```bash
# Lấy messages của một chat
curl -X GET "http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/messages?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Gửi message mới
curl -X POST http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Hello from API test!",
    "type": "TEXT"
  }'
```

### Pinned Message APIs
```bash
# Ghim một tin nhắn
curl -X POST http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/pinned-messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "750e8400-e29b-41d4-a716-446655440001",
    "displayOrder": 1
  }'

# Lấy danh sách tin nhắn đã ghim
curl -X GET http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/pinned-messages \
  -H "Authorization: Bearer $TOKEN"

# Bỏ ghim một tin nhắn
curl -X DELETE http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/pinned-messages/750e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer $TOKEN"

# Kiểm tra xem tin nhắn có được ghim không
curl -X GET http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/pinned-messages/750e8400-e29b-41d4-a716-446655440001/is-pinned \
  -H "Authorization: Bearer $TOKEN"
```

### Participant APIs
```bash
# Lấy danh sách participants
curl -X GET http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/participants \
  -H "Authorization: Bearer $TOKEN"

# Thêm participant mới
curl -X POST http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/participants \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440009",
    "role": "MEMBER"
  }'
```

---

## Test Scenarios

### Scenario 1: User Search Flow
```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "123456"}' | \
  jq -r '.data.accessToken')

# 2. Search for users
curl -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "alice", "limit": 5}'

# 3. Save search history
curl -X POST http://localhost:8080/api/v1/users/search/history/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer $TOKEN"

# 4. Get recent searches
curl -X GET http://localhost:8080/api/v1/users/search/recent \
  -H "Authorization: Bearer $TOKEN"
```

### Scenario 2: Contact Management Flow
```bash
# 1. Get suggested contacts
curl -X GET http://localhost:8080/api/v1/users/search/suggested \
  -H "Authorization: Bearer $TOKEN"

# 2. Add a contact
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contactUserId": "550e8400-e29b-41d4-a716-446655440005", "displayName": "Eva Martinez"}'

# 3. Get all contacts
curl -X GET http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN"

# 4. Update contact display name
curl -X PUT http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440005 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"displayName": "Eva M. (Product Owner)"}'
```

---

## Response Examples

### User Search Response
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "username": "alice_johnson",
      "email": "alice@example.com",
      "avatarUrl": "https://i.pravatar.cc/150?img=1",
      "presenceStatus": "ONLINE",
      "lastSeenAt": "2024-10-21T10:30:00Z",
      "isContact": false,
      "mutualContactsCount": 2,
      "displayName": null
    }
  ],
  "message": "Tìm kiếm người dùng thành công"
}
```

### Contact Response
```json
{
  "success": true,
  "data": {
    "id": "contact-uuid",
    "userId": "current-user-id",
    "contactUserId": "550e8400-e29b-41d4-a716-446655440002",
    "displayName": "Bob Smith",
    "username": "bob_smith",
    "email": "bob@example.com",
    "avatarUrl": "https://i.pravatar.cc/150?img=2",
    "presenceStatus": "OFFLINE",
    "lastSeenAt": "2024-10-21T08:30:00Z",
    "mutualContactsCount": 3,
    "createdAt": "2024-10-21T10:00:00Z",
    "updatedAt": "2024-10-21T10:00:00Z"
  },
  "message": "Thêm liên hệ thành công"
}
```

---

## Error Handling Examples

### Invalid Token
```bash
curl -X GET http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer invalid_token"
```

### User Not Found
```bash
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contactUserId": "00000000-0000-0000-0000-000000000000"}'
```

### Duplicate Contact
```bash
# Try adding the same contact twice
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contactUserId": "550e8400-e29b-41d4-a716-446655440002"}'
```

---

## Notes

- Replace `$TOKEN` with actual JWT token from login response
- All UUIDs in examples are from sample data
- Server should be running on `http://localhost:8080`
- Use `jq` for JSON parsing in bash scripts
- Check response status codes for error handling