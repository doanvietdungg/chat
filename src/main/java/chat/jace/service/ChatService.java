package chat.jace.service;

import chat.jace.domain.Chat;
import chat.jace.domain.Participant;
import chat.jace.domain.enums.ChatType;
import chat.jace.domain.enums.ParticipantRole;
import chat.jace.dto.chat.ChatCreateRequest;
import chat.jace.dto.chat.ChatResponse;
import chat.jace.dto.chat.ChatUpdateRequest;
import chat.jace.repository.ChatRepository;
import chat.jace.repository.ParticipantRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Page<ChatResponse> listMyChats(Pageable pageable) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        var parts = participantRepository.findByUserId(me);
        var chatIds = parts.stream().map(Participant::getChatId).toList();
        var chats = chatRepository.findAllById(chatIds);
        // simple in-memory pagination for MVP
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), chats.size());
        List<ChatResponse> content = chats.subList(Math.min(start, chats.size()), end)
                .stream().map(ChatService::toResponse).toList();
        return new PageImpl<>(content, pageable, chats.size());
    }

    @Transactional
    public ChatResponse create(ChatCreateRequest req) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        Chat chat = Chat.builder()
                .type(req.getType() == null ? ChatType.PRIVATE : req.getType())
                .title(req.getTitle())
                .description(req.getDescription())
                .createdBy(me)
                .build();
        chat = chatRepository.save(chat);
        participantRepository.save(Participant.builder()
                .chatId(chat.getId())
                .userId(me)
                .role(ParticipantRole.OWNER)
                .build());
        messagingTemplate.convertAndSend("/topic/chat.updated", toResponse(chat));
        return toResponse(chat);
    }

    public ChatResponse get(UUID chatId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireMember(chatId, me);
        return toResponse(chatRepository.findById(chatId).orElseThrow());
    }

    @Transactional
    public ChatResponse update(UUID chatId, ChatUpdateRequest req) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireAdminOrOwner(chatId, me);
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        if (req.getTitle() != null) chat.setTitle(req.getTitle());
        if (req.getDescription() != null) chat.setDescription(req.getDescription());
        if (req.getSettings() != null) chat.setSettings(req.getSettings());
        chat = chatRepository.save(chat);
        var resp = toResponse(chat);
        messagingTemplate.convertAndSend("/topic/chats/"+chatId+"/updated", resp);
        return resp;
    }

    @Transactional
    public void deleteOrLeave(UUID chatId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireMember(chatId, me);
        // owners delete, others leave
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        if (isOwner(chatId, me)) {
            chatRepository.deleteById(chatId);
            messagingTemplate.convertAndSend("/topic/chats/"+chatId+"/deleted", chatId);
        } else {
            participantRepository.deleteById(new Participant.ParticipantId(chatId, me));
        }
    }

    private void requireMember(UUID chatId, UUID userId) {
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new IllegalArgumentException("Not a chat participant");
        }
    }

    private static ChatResponse toResponse(Chat chat) {
        return ChatResponse.builder()
                .id(chat.getId())
                .type(chat.getType())
                .title(chat.getTitle())
                .description(chat.getDescription())
                .createdBy(chat.getCreatedBy())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .build();
    }

    private boolean isOwner(UUID chatId, UUID userId) {
        return participantRepository.findByChatIdAndUserId(chatId, userId)
                .map(p -> p.getRole() == ParticipantRole.OWNER)
                .orElse(false);
    }

    private void requireAdminOrOwner(UUID chatId, UUID userId) {
        Participant p = participantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Not a chat participant"));
        if (p.getRole() == ParticipantRole.MEMBER) {
            throw new IllegalArgumentException("Admin or owner required");
        }
    }
}
