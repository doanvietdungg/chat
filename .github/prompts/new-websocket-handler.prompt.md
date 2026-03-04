---
agent: 'agent'
tools: ['codebase']
description: 'Tạo WebSocket STOMP handler mới theo convention của dự án Jace'
---

# Tạo WebSocket STOMP Handler

Tạo WebSocket handler mới cho dự án Jace (STOMP + SockJS).

## Yêu cầu

Hãy cung cấp:
- **Event/Action**: Ví dụ `send-message`, `typing-indicator`, `read-receipt`
- **Destination**: Ví dụ `/app/chat/{chatId}/send`
- **Broadcast target**: Ví dụ `/topic/{chatId}` (room) hoặc `/queue/{userId}` (private)

## Architecture WebSocket trong Jace

```
Client → STOMP CONNECT (với JWT header)
       → @MessageMapping("/app/...") trong controller/ws/
       → Service xử lý logic
       → SimpMessagingTemplate.convertAndSend("/topic/...") broadcast
```

## Rules bắt buộc

### Controller (controller/ws/)
```java
@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    // Constructor injection
    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(@DestinationVariable UUID chatId,
                            @Payload SendMessageRequest request,
                            Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());
        var response = messageService.sendMessage(chatId, senderId, request);

        // Broadcast tới room
        messagingTemplate.convertAndSend("/topic/" + chatId, response);

        // Hoặc gửi private tới user cụ thể
        // messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/messages", response);
    }
}
```

### Auth trong WebSocket
- **KHÔNG** dùng `SecurityUtils.currentUserIdOrThrow()` trong WS handler
- Dùng `Principal principal` parameter — đã được inject bởi `StompAuthChannelInterceptor`
- Extract userId: `UUID.fromString(principal.getName())`

### Destination conventions
| Pattern | Dùng khi |
|---|---|
| `/topic/{chatId}` | Broadcast tới tất cả member trong chat room |
| `/queue/{userId}` | Gửi tới 1 user cụ thể |
| `/user/{userId}/queue/...` | User-specific với Spring Security principal |

### Payload DTO
- Dùng Java **Record** cho payload:
```java
public record SendMessageRequest(
    @NotBlank String content,
    String messageType   // TEXT, IMAGE, FILE
) {}
```

### Error handling trong WebSocket
```java
@MessageExceptionHandler
@SendToUser("/queue/errors")
public ErrorResponse handleException(Exception ex, Principal principal) {
    return new ErrorResponse(ex.getMessage());
}
```

### Checklist
- [ ] Handler class đặt trong `controller/ws/`
- [ ] Dùng `Principal` để lấy userId (không dùng `SecurityUtils`)
- [ ] Dùng constructor injection
- [ ] Broadcast destination đúng pattern `/topic/` hoặc `/queue/`
- [ ] Payload dùng Record + validation
- [ ] Có `@MessageExceptionHandler` để xử lý lỗi

## Test với TestSocket.html
Mở `src/main/resources/templates/TestSocket.html` để test WebSocket thủ công.
