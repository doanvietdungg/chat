package chat.jace.service;

import chat.jace.domain.Message;
import chat.jace.domain.PinnedMessage;
import chat.jace.dto.message.MessageResponse;
import chat.jace.dto.message.PinnedMessageResponse;
import chat.jace.repository.MessageRepository;
import chat.jace.repository.PinnedMessageRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PinnedMessageService {

    private final PinnedMessageRepository pinnedMessageRepository;
    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Pin a message in a chat
     */
    @Transactional
    public PinnedMessageResponse pinMessage(UUID chatId, UUID messageId, Integer displayOrder) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        
        // Verify user is member of the chat
        chatService.requireMember(chatId, me);
        
        // Verify message exists and belongs to this chat
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (!message.getChatId().equals(chatId)) {
            throw new IllegalArgumentException("Message does not belong to this chat");
        }
        
        // Check if already pinned
        if (pinnedMessageRepository.existsByIdChatIdAndIdMessageId(chatId, messageId)) {
            throw new IllegalArgumentException("Message is already pinned");
        }
        
        // Optional: Limit number of pinned messages per chat
        long pinnedCount = pinnedMessageRepository.countByIdChatId(chatId);
        if (pinnedCount >= 10) { // Max 10 pinned messages
            throw new IllegalArgumentException("Maximum number of pinned messages reached");
        }
        
        // Create pinned message
        PinnedMessage pinnedMessage = PinnedMessage.builder()
                .id(new PinnedMessage.PinnedMessageId(chatId, messageId))
                .pinnedBy(me)
                .displayOrder(displayOrder)
                .build();
        
        pinnedMessage = pinnedMessageRepository.save(pinnedMessage);
        
        // Notify all participants via WebSocket
        PinnedMessageResponse response = toResponse(pinnedMessage, message);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/pinned", 
                java.util.Map.of(
                        "type", "message.pinned",
                        "payload", response
                ));
        
        return response;
    }

    /**
     * Unpin a message
     */
    @Transactional
    public void unpinMessage(UUID chatId, UUID messageId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        
        // Verify user is member of the chat
        chatService.requireMember(chatId, me);
        
        // Check if message is pinned
        if (!pinnedMessageRepository.existsByIdChatIdAndIdMessageId(chatId, messageId)) {
            throw new IllegalArgumentException("Message is not pinned");
        }
        
        // Delete pinned message
        pinnedMessageRepository.deleteByIdChatIdAndIdMessageId(chatId, messageId);
        
        // Notify all participants via WebSocket
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/pinned", 
                java.util.Map.of(
                        "type", "message.unpinned",
                        "payload", java.util.Map.of(
                                "chatId", chatId,
                                "messageId", messageId
                        )
                ));
    }

    /**
     * Get all pinned messages in a chat
     */
    public List<PinnedMessageResponse> getPinnedMessages(UUID chatId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        
        // Verify user is member of the chat
        chatService.requireMember(chatId, me);
        
        List<PinnedMessage> pinnedMessages = pinnedMessageRepository.findByChatIdOrderByDisplayOrder(chatId);
        
        return pinnedMessages.stream()
                .map(pm -> {
                    Message message = messageRepository.findById(pm.getId().getMessageId())
                            .orElse(null);
                    return toResponse(pm, message);
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if a message is pinned
     */
    public boolean isPinned(UUID chatId, UUID messageId) {
        return pinnedMessageRepository.existsByIdChatIdAndIdMessageId(chatId, messageId);
    }

    private PinnedMessageResponse toResponse(PinnedMessage pinnedMessage, Message message) {
        MessageResponse messageResponse = null;
        if (message != null) {
            messageResponse = messageService.toResponse(message);
        }
        
        return PinnedMessageResponse.builder()
                .chatId(pinnedMessage.getId().getChatId())
                .messageId(pinnedMessage.getId().getMessageId())
                .pinnedBy(pinnedMessage.getPinnedBy())
                .pinnedAt(pinnedMessage.getPinnedAt())
                .displayOrder(pinnedMessage.getDisplayOrder())
                .message(messageResponse)
                .build();
    }
}
