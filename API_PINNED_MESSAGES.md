Pinned Messages API — cURL & Postman examples

Mô tả: các ví dụ cURL và hướng dẫn nhanh để tạo request trong Postman cho các endpoint quản lý pinned messages (dựa trên PinnedMessageController.java).

BASE_URL: http://localhost:8080
Headers chung:
- Authorization: Bearer <ACCESS_TOKEN>
- Content-Type: application/json

1) Pin a message (POST /api/v1/chats/{chatId}/pinned-messages)

cURL:

curl -X POST "${BASE_URL}/api/v1/chats/{chatId}/pinned-messages" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "11111111-1111-1111-1111-111111111111",
    "displayOrder": 1
  }'

Postman (quick):
- Method: POST
- URL: {{BASE_URL}}/api/v1/chats/{{chatId}}/pinned-messages
- Headers: Authorization: Bearer {{ACCESS_TOKEN}}, Content-Type: application/json
- Body (raw, JSON):
{
  "messageId": "11111111-1111-1111-1111-111111111111",
  "displayOrder": 1
}

2) Unpin a message (DELETE /api/v1/chats/{chatId}/pinned-messages/{messageId})

cURL:

curl -X DELETE "${BASE_URL}/api/v1/chats/{chatId}/pinned-messages/{messageId}" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

Postman (quick):
- Method: DELETE
- URL: {{BASE_URL}}/api/v1/chats/{{chatId}}/pinned-messages/{{messageId}}
- Headers: Authorization: Bearer {{ACCESS_TOKEN}}

3) Get all pinned messages (GET /api/v1/chats/{chatId}/pinned-messages)

cURL:

curl -X GET "${BASE_URL}/api/v1/chats/{chatId}/pinned-messages" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

Postman (quick):
- Method: GET
- URL: {{BASE_URL}}/api/v1/chats/{{chatId}}/pinned-messages
- Headers: Authorization: Bearer {{ACCESS_TOKEN}}

4) Check if a message is pinned (GET /api/v1/chats/{chatId}/pinned-messages/{messageId}/is-pinned)

cURL:

curl -X GET "${BASE_URL}/api/v1/chats/{chatId}/pinned-messages/{messageId}/is-pinned" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

Postman (quick):
- Method: GET
- URL: {{BASE_URL}}/api/v1/chats/{{chatId}}/pinned-messages/{{messageId}}/is-pinned
- Headers: Authorization: Bearer {{ACCESS_TOKEN}}

Notes:
- Thay {chatId} và {messageId} bằng UUID thực tế.
- Thay <ACCESS_TOKEN> bằng token hợp lệ (JWT) nếu API yêu cầu xác thực.
- Để import cURL vào Postman: trong Postman chọn Import -> Raw Text -> dán câu lệnh cURL -> Import.

End of file.

