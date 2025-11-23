package chat.jace.service;

import chat.jace.domain.Message;
import chat.jace.domain.MessageRead;
import chat.jace.repository.MessageReadRepository;
import chat.jace.repository.MessageRepository;
import chat.jace.repository.ParticipantRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReadReceiptService {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final ParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void markRead(UUID messageId, UUID userId) {
        Message msg = messageRepository.findById(messageId).orElseThrow();
        requireMember(msg.getChatId(), userId);
        if (!messageReadRepository.existsByMessageIdAndUserId(messageId, userId)) {
            messageReadRepository.save(MessageRead.builder().messageId(messageId).userId(userId).build());
            messagingTemplate.convertAndSend("/topic/chats/" + msg.getChatId() + "/events",
                    Map.of("type", "message.read", "payload", Map.of("messageId", messageId.toString(), "userId", userId.toString())));
        }
    }

    private void requireMember(UUID chatId, UUID userId) {
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new IllegalArgumentException("Not a chat participant");
        }
    }
}
