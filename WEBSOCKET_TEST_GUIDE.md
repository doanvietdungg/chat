# WebSocket Test Interface - Hướng dẫn sử dụng

## Truy cập giao diện test

Sau khi khởi động server, truy cập:
```
http://localhost:8080/test/websocket
```

## Chuẩn bị

### 1. Lấy JWT Token

Trước tiên cần đăng ký/đăng nhập để lấy JWT token:

**Đăng ký user mới:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Hoặc đăng nhập:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }'
```

Response sẽ trả về:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "tokenType": "Bearer"
}
```

Copy giá trị `accessToken` (không cần thêm "Bearer").

### 2. Tạo Chat để test

```bash
curl -X POST http://localhost:8080/api/v1/chats \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PRIVATE",
    "title": "Test Chat"
  }'
```

Response sẽ trả về chat với `id` (UUID). Lưu lại ID này.

## Sử dụng giao diện test

### Bước 1: Kết nối WebSocket

1. Mở giao diện test tại `http://localhost:8080/test/websocket`
2. Paste JWT token vào ô "JWT Token"
3. Nhấn nút **Connect**
4. Kiểm tra logs, nếu thấy "✅ Connected successfully!" là thành công

### Bước 2: Subscribe vào Chat

1. Nhập Chat ID vào ô "Chat ID" (ví dụ: `1` hoặc UUID của chat đã tạo)
2. Nhấn nút **Subscribe to Chat**
3. Logs sẽ hiển thị: "✅ Subscribed to chat {chatId}"

### Bước 3: Gửi tin nhắn

1. Nhập nội dung vào ô "Message Text"
2. Nhấn nút **Send to Chat**
3. Tin nhắn sẽ được gửi và bạn sẽ nhận lại tin nhắn qua subscription

### Test với 2 users

Để test đầy đủ, mở 2 tab browser:

**Tab 1 (User A):**
1. Đăng nhập với user A, lấy token
2. Connect WebSocket với token của A
3. Subscribe vào chat ID = 1

**Tab 2 (User B):**
1. Đăng nhập với user B, lấy token
2. Connect WebSocket với token của B
3. Subscribe vào chat ID = 1

Bây giờ khi User A gửi tin nhắn, User B sẽ nhận được real-time!

## Các tính năng test

### 1. Send Message (Gửi tin nhắn vào chat)
- Điền Chat ID và Message Text
- Nhấn "Send to Chat"
- Endpoint: `/app/messages.send`
- Payload: `{ chatId, text, type: "TEXT" }`

### 2. First Message (Tin nhắn đầu tiên - chưa có chat)
- Điền Recipient ID (UUID của user nhận)
- Điền Message Text
- Nhấn "Send First Message"
- Endpoint: `/app/messages.send`
- Payload: `{ recipientId, text, type: "TEXT" }`
- Tin nhắn sẽ được route đến `/user/{recipientId}/events`

### 3. Typing Indicator (Hiển thị đang gõ)
- Tự động gửi khi nhập vào ô "Message Text"
- Endpoint: `/app/typing`
- Payload: `{ chatId, typing: true/false }`

### 4. Presence (Trạng thái online/offline)
- Chọn status: ONLINE, AWAY, BUSY, OFFLINE
- Nhấn "Update Presence"
- Endpoint: `/app/presence.update`
- Payload: `{ status }`

### 5. Heartbeat
- Nhấn "Send Heartbeat" để báo server user còn online
- Endpoint: `/app/presence.heartbeat`
- Payload: `{}`

### 6. Read Receipt (Đánh dấu đã đọc)
- Điền Chat ID và Message ID
- Nhấn "Send Read Receipt"
- Endpoint: `/app/read`
- Payload: `{ chatId, messageId }`

## WebSocket Endpoints Summary

### Subscriptions (Nhận tin)
- `/topic/chats/{chatId}/messages` - Nhận tin nhắn mới
- `/topic/chats/{chatId}/typing` - Nhận typing indicator
- `/user/events` - Nhận sự kiện riêng của user (auto-subscribe khi connect)

### Sends (Gửi tin)
- `/app/messages.send` - Gửi tin nhắn
- `/app/typing` - Gửi typing indicator
- `/app/presence.update` - Cập nhật presence
- `/app/presence.heartbeat` - Gửi heartbeat
- `/app/read` - Đánh dấu đã đọc

## Troubleshooting

### Lỗi "Not connected"
- Kiểm tra đã nhập JWT token chưa
- Kiểm tra token còn hạn không (JWT có thể expire)
- Kiểm tra server đang chạy tại localhost:8080

### Không nhận được tin nhắn
- Kiểm tra đã subscribe vào chat chưa (nhấn "Subscribe to Chat")
- Kiểm tra Chat ID đúng chưa
- Kiểm tra user có quyền truy cập chat không (phải là participant)

### Lỗi "Connection error"
- Kiểm tra JWT token hợp lệ
- Kiểm tra server logs để xem lỗi chi tiết
- Kiểm tra WebSocket endpoint `/ws` có hoạt động không

### Token expired
- Gọi lại API `/auth/login` hoặc `/auth/refresh` để lấy token mới
- Copy token mới và Connect lại

## Logs

Giao diện có log console với các icon:
- ✅ Success (kết nối thành công, gửi thành công)
- ❌ Error (lỗi kết nối, thiếu thông tin)
- 💬 Message (nhận tin nhắn, sự kiện)
- ℹ️ Info (thông tin chung)

Nhấn "Clear Logs" để xóa logs.

## API Reference

Chi tiết đầy đủ các API xem tại:
- `API_SUMMARY.md` - Tổng quan API
- `API_CURL_EXAMPLES.md` - Ví dụ curl commands
- Swagger UI: `http://localhost:8080/swagger-ui.html`
