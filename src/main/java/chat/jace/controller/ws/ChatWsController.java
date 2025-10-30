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
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
            log.warn("Received message from unauthenticated user, ignoring");
            return; // ignore unauthenticated messages
        }
        
        Message msg = null;
        
        // If chatId exists: normal chat message, persist and broadcast to chat topic
        if (payload.getChatId() != null) {
            msg = Message.builder()
                    .chatId(payload.getChatId())
                    .authorId(UUID.fromString(userId))
                    .text(payload.getText())
                    .type(payload.getType() == null ? MessageType.TEXT : payload.getType())
                    .build();
            msg = messageRepository.save(msg);
            messagingTemplate.convertAndSend("/topic/chats/" + payload.getChatId() + "/messages", msg);
        }

        // If recipientId exists: also send to recipient's user channel
        UUID recipientId = payload.getRecipientId();
        if (recipientId != null) {
            Message eventMsg = msg != null ? msg : Message.builder()
                    .chatId(null)
                    .authorId(UUID.fromString(userId))
                    .text(payload.getText())
                    .type(payload.getType() == null ? MessageType.TEXT : payload.getType())
                    .build();
            
            String recipientIdStr = recipientId.toString();
            
            // Try both methods: user-specific and topic broadcast
            log.info("Attempting to send message to user: {}", recipientIdStr);
            log.info("Message payload: type={}, text={}", msg != null ? "message.sent" : "message.first", eventMsg.getText());
            try {
                messagingTemplate.convertAndSendToUser(recipientIdStr, "/topic/events",
                        java.util.Map.of(
                                "type", msg != null ? "message.sent" : "message.first",
                                "payload", eventMsg
                        ));
                log.info("Sent to /topic/user/{}/events", recipientIdStr);
            } catch (Exception e) {
                log.error("Failed to send to topic: {}", e.getMessage());
            }
        }
    }

//    @MessageMapping("/typing")
//    public void typing(@Valid TypingPayload payload, Principal principal) {
//        String userId = principal != null ? principal.getName() : null;
//        if (userId == null) return;
//        Map<String, Object> evt = new HashMap<>();
//        evt.put("userId", userId);
//        evt.put("typing", payload.getTyping());
//        messagingTemplate.convertAndSend("/topic/chats/" + payload.getChatId() + "/typing", evt);
//    }

    @MessageMapping("/read")
    public void read(@Valid ReadReceiptPayload payload, Principal principal) {
        String userId = principal != null ? principal.getName() : null;
        if (userId == null) return;
        // Delegate to service (persists and broadcasts)
        readReceiptService.markRead(payload.getMessageId());
    }
}
