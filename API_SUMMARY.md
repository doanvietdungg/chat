# Chat Backend API Summary

Base URL: `http://localhost:8080`

Auth: Bearer JWT in `Authorization: Bearer <token>` (except where noted).

## Auth
- **POST** `/auth/register`
  - Body: `{ "username": string, "email": string, "password": string }`
  - Resp: `{ accessToken, refreshToken, tokenType }`
  - Auth: No

- **POST** `/auth/login`
  - Body: `{ "usernameOrEmail": string, "password": string }`
  - Resp: `{ accessToken, refreshToken, tokenType }`
  - Auth: No

- **POST** `/auth/refresh`
  - Body: `{ "refreshToken": string }`
  - Resp: `{ accessToken, refreshToken, tokenType }`
  - Auth: No

## Chats
- **GET** `/chats`
  - Query: pagination via Spring `Pageable` (`page`, `size`, `sort`)
  - Resp: `Page<ChatResponse>`
  - Auth: Yes

- **POST** `/chats`
  - Body: `{ "type": "PRIVATE|GROUP|CHANNEL", "title"?: string, "description"?: string }`
  - Resp: `ChatResponse`
  - Auth: Yes

- **GET** `/chats/{id}`
  - Resp: `ChatResponse`
  - Auth: Yes (participant-only)

- **PUT** `/chats/{id}`
  - Body: `{ "title"?: string, "description"?: string, "settings"?: string(json) }`
  - Resp: `ChatResponse`
  - Auth: Yes (owner/admin)

- **DELETE** `/chats/{id}`
  - Resp: `204 No Content`
  - Auth: Yes (owner deletes; others leave)

`ChatResponse`:
```
{
  id: UUID,
  type: "PRIVATE|GROUP|CHANNEL",
  title: string|null,
  description: string|null,
  createdBy: UUID,
  createdAt: string,
  updatedAt: string
}
```

## Messages
- **GET** `/chats/{chatId}/messages`
  - Query: pagination via `Pageable`
  - Resp: `Page<MessageResponse>`
  - Auth: Yes (participant-only)

- **POST** `/chats/{chatId}/messages`
  - Body: `{ "text": string, "type": "TEXT|IMAGE|FILE|SYSTEM" }`
  - Resp: `MessageResponse`
  - Auth: Yes (participant-only)

- **PUT** `/messages/{id}`
  - Body: `{ "text": string }`
  - Resp: `MessageResponse`
  - Auth: Yes (author-only)

- **DELETE** `/messages/{id}`
  - Resp: `204 No Content`
  - Auth: Yes (author-only)

- **POST** `/messages/{id}/read`
  - Resp: `{ status: "ok" }`
  - Auth: Yes (participant-only)

`MessageResponse`:
```
{
  id: UUID,
  chatId: UUID,
  authorId: UUID,
  text: string|null,
  type: "TEXT|IMAGE|FILE|SYSTEM",
  fileId: UUID|null,
  createdAt: string,
  updatedAt: string
}
```

## Participants
- **GET** `/chats/{chatId}/participants`
  - Resp: `ParticipantResponse[]`
  - Auth: Yes (participant-only)

- **POST** `/chats/{chatId}/participants`
  - Body: `{ "userId": UUID, "role": "MEMBER|ADMIN" }`
  - Resp: `ParticipantResponse`
  - Auth: Yes (owner/admin)

- **PUT** `/chats/{chatId}/participants/{userId}`
  - Body: `{ "role": "MEMBER|ADMIN" }`
  - Resp: `ParticipantResponse`
  - Auth: Yes (owner-only; cannot assign OWNER)

- **DELETE** `/chats/{chatId}/participants/{userId}`
  - Resp: `204 No Content`
  - Auth: Yes (owner; or admin can remove members)

`ParticipantResponse`:
```
{
  chatId: UUID,
  userId: UUID,
  role: "OWNER|ADMIN|MEMBER",
  joinedAt: string
}
```

## Files
- **POST** `/files/upload`
  - Form: `file` (multipart)
  - Resp: `FileResource`
  - Auth: Yes

- **GET** `/files/{id}`
  - Resp: `FileResource`
  - Auth: Yes

- **DELETE** `/files/{id}`
  - Resp: `{ status: "deleted" }`
  - Auth: Yes

- **GET** `/files/raw/{storedName}`
  - Resp: binary content
  - Auth: Public (consider protecting if needed)

`FileResource` (subset):
```
{
  id: UUID,
  name: string,
  size: number,
  contentType: string|null,
  url: string,            // e.g. /files/raw/<storedName>
  uploadedBy: UUID|null,
  createdAt: string
}
```

## WebSocket (STOMP over WebSocket)
- Endpoint: `/ws` (SockJS)
- App destination prefix: `/app`
- Broker: `/topic`, `/queue`
- User prefix: `/user`
- Auth: Include header `Authorization: Bearer <token>` on CONNECT

### Subscriptions
- `/topic/chats/{chatId}` — message broadcasts
- `/topic/chats/{chatId}/typing` — typing indicators
- `/topic/chats/{chatId}/events` — system events:
  - `message.sent`, `message.updated`, `message.deleted`
  - `message.read`
  - `user.online`, `user.offline`
  - `participant.added`, `participant.updated`, `participant.removed`

### Sends
- `/app/messages.send`
```
{ "chatId": UUID, "text": string, "type": "TEXT|IMAGE|FILE|SYSTEM" }
```

- `/app/typing`
```
{ "chatId": UUID, "typing": true|false }
```

- `/app/read`
```
{ "chatId": UUID, "messageId": UUID }
```

## Health & Docs
- **GET** `/actuator/health`
- **GET** `/v3/api-docs`
- **GET** `/swagger-ui.html`

## Notes
- Role rules:
  - Chat update: owner/admin
  - Chat delete: owner; others delete means leaving chat
  - Participants: owner/admin add; owner updates roles; owner/admin remove (admins cannot remove admins/owner)
- Rate limiting: `/auth/**` protected via Redis-based window (`app.rate-limit.auth.*`).
- CORS: configured via `app.cors.*` properties.
- WebSocket broker: embedded or RabbitMQ (`app.ws.broker`).
