package chat.jace.service;

import chat.jace.domain.FileResource;
import chat.jace.domain.Message;
import chat.jace.domain.enums.FileOwnerType;
import chat.jace.domain.enums.MessageType;
import chat.jace.dto.file.FileResponse;
import chat.jace.dto.message.MessageCreateRequest;
import chat.jace.dto.message.MessageResponse;
import chat.jace.repository.MessageRepository;
import chat.jace.repository.ParticipantRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileStorageService fileStorageService;

    public Page<MessageResponse> list(UUID chatId, Pageable pageable) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireMember(chatId, me);
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public MessageResponse send(MessageCreateRequest req) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireMember(req.getChatId(), me);
        
        // Validate: either text or fileId must be provided
        if ((req.getText() == null || req.getText().isBlank()) && req.getFileId() == null) {
            throw new IllegalArgumentException("Either text or fileId must be provided");
        }
        
        Message msg = Message.builder()
                .chatId(req.getChatId())
                .authorId(me)
                .text(req.getText())
                .type(req.getType() == null ? MessageType.TEXT : req.getType())
                .fileId(req.getFileId())
                .build();
        msg = messageRepository.save(msg);
        
        // If fileId is provided, update file owner to this message
        if (req.getFileId() != null) {
            Message finalMsg = msg;
            fileStorageService.get(req.getFileId()).ifPresent(file -> {
                fileStorageService.setOwner(file.getId(), FileOwnerType.MESSAGE, finalMsg.getId());
            });
        }
        
        var resp = toResponse(msg);
        messagingTemplate.convertAndSend("/topic/chats/" + req.getChatId(), resp);
        messagingTemplate.convertAndSend("/topic/chats/" + req.getChatId() + "/events", event("message.sent", resp));
        return resp;
    }

    @Transactional
    public MessageResponse update(UUID messageId, String newText) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        Message msg = messageRepository.findById(messageId).orElseThrow();
        requireMember(msg.getChatId(), me);
        if (!me.equals(msg.getAuthorId())) {
            throw new IllegalArgumentException("Only author can edit message");
        }
        msg.setText(newText);
        msg = messageRepository.save(msg);
        var resp = toResponse(msg);
        messagingTemplate.convertAndSend("/topic/chats/" + msg.getChatId() + "/events", event("message.updated", resp));
        return resp;
    }

    @Transactional
    public void delete(UUID messageId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        Message msg = messageRepository.findById(messageId).orElseThrow();
        requireMember(msg.getChatId(), me);
        if (!me.equals(msg.getAuthorId())) {
            throw new IllegalArgumentException("Only author can delete message");
        }
        messageRepository.deleteById(messageId);
        messagingTemplate.convertAndSend("/topic/chats/" + msg.getChatId() + "/events", event("message.deleted", messageId));
    }

    private void requireMember(UUID chatId, UUID userId) {
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new IllegalArgumentException("Not a chat participant");
        }
    }

    public MessageResponse toResponse(Message m) {
        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                .id(m.getId())
                .chatId(m.getChatId())
                .authorId(m.getAuthorId())
                .text(m.getText())
                .type(m.getType())
                .fileId(m.getFileId())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt());
        
        // Load file info if fileId exists
        if (m.getFileId() != null) {
            fileStorageService.get(m.getFileId()).ifPresent(file -> {
                builder.file(toFileResponse(file));
            });
        }
        
        return builder.build();
    }
    
    private FileResponse toFileResponse(FileResource file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .size(file.getSize())
                .contentType(file.getContentType())
                .url(file.getUrl())
                .uploadedBy(file.getUploadedBy())
                .ownerType(file.getOwnerType())
                .ownerId(file.getOwnerId())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private static Object event(String type, Object payload) {
        return java.util.Map.of("type", type, "payload", payload);
    }
}
