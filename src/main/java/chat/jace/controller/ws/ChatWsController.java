package chat.jace.controller.ws;

import chat.jace.domain.Message;
import chat.jace.domain.enums.MessageType;
import chat.jace.dto.ws.SendMessagePayload;
import chat.jace.dto.ws.TypingPayload;
import chat.jace.dto.ws.ReadReceiptPayload;
import chat.jace.repository.MessageRepository;
import chat.jace.service.ReadReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ReadReceiptService readReceiptService;

    @MessageMapping("/messages.send")
    public void sendMessage(@Valid SendMessagePayload payload, Principal principal, SimpMessageHeaderAccessor headers) {
        String userId = principal != null ? principal.getName() : null; // set by StompAuthChannelInterceptor
        if (userId == null) {
            return; // ignore unauthenticated messages
        }
        Message msg = Message.builder()
                .chatId(payload.getChatId())
                .authorId(UUID.fromString(userId))
                .text(payload.getText())
                .type(payload.getType() == null ? MessageType.TEXT : payload.getType())
                .build();
        msg = messageRepository.save(msg);

        messagingTemplate.convertAndSend("/topic/chats/" + payload.getChatId(), msg);
    }

    @MessageMapping("/typing")
    public void typing(@Valid TypingPayload payload, Principal principal) {
        String userId = principal != null ? principal.getName() : null;
        if (userId == null) return;
        Map<String, Object> evt = new HashMap<>();
        evt.put("userId", userId);
        evt.put("typing", payload.getTyping());
        messagingTemplate.convertAndSend("/topic/chats/" + payload.getChatId() + "/typing", evt);
    }

    @MessageMapping("/read")
    public void read(@Valid ReadReceiptPayload payload, Principal principal) {
        String userId = principal != null ? principal.getName() : null;
        if (userId == null) return;
        // Delegate to service (persists and broadcasts)
        readReceiptService.markRead(payload.getMessageId());
    }
}
