package chat.jace.service;

import chat.jace.domain.Chat;
import chat.jace.domain.Participant;
import chat.jace.domain.User;
import chat.jace.domain.enums.ChatType;
import chat.jace.domain.enums.ParticipantRole;
import chat.jace.dto.chat.ChatCreateRequest;
import chat.jace.dto.chat.ChatParticipantInfo;
import chat.jace.dto.chat.ChatResponse;
import chat.jace.dto.chat.ChatUpdateRequest;
import chat.jace.repository.ChatRepository;
import chat.jace.repository.ParticipantRepository;
import chat.jace.repository.UserRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import chat.jace.dto.participant.ParticipantAddRequest;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
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
                .stream().map(chat -> toResponse(chat, me)).toList();
        return new PageImpl<>(content, pageable, chats.size());
    }

    @Transactional
    public ChatResponse create(ChatCreateRequest req) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        ChatType type = req.getType() == null ? ChatType.PRIVATE : req.getType();

        validateGroupTitle(type, req.getTitle());

        if (type == ChatType.PRIVATE && req.getOtherUserId() != null) {
            Optional<ChatResponse> existing = findExistingPrivateChat(me, req.getOtherUserId());
            if (existing.isPresent()) return existing.get();
        }

        Chat chat = buildAndSaveChat(req, type, me);

        Set<UUID> recipients = new HashSet<>();
        recipients.add(me);
        addOwnerParticipant(chat.getId(), me);
        addPrivateParticipantIfNeeded(chat.getId(), type, req.getOtherUserId(), recipients);
        addGroupParticipantsIfNeeded(chat.getId(), me, req.getParticipants(), recipients);

        var resp = toResponse(chat, me);
        notifyChatCreated(resp, recipients);
        return resp;
    }

    /**
     * Validates that GROUP and CHANNEL chats have a non-blank title.
     *
     * @param type  the chat type being created
     * @param title the requested title
     * @throws IllegalArgumentException if the title is missing for a GROUP or CHANNEL
     */
    private void validateGroupTitle(ChatType type, String title) {
        if ((type == ChatType.GROUP || type == ChatType.CHANNEL)
                && (title == null || title.isBlank())) {
            throw new IllegalArgumentException("Title is required for group or channel");
        }
    }

    /**
     * Searches for an existing PRIVATE chat between two users to avoid duplicates.
     *
     * @param me    the current user's ID
     * @param other the other user's ID
     * @return an {@link Optional} containing the existing {@link ChatResponse}, or empty if none found
     */
    private Optional<ChatResponse> findExistingPrivateChat(UUID me, UUID other) {
        var myParts = participantRepository.findByUserId(me);
        for (Participant part : myParts) {
            var existingOpt = chatRepository.findById(part.getChatId());
            if (existingOpt.isPresent() && existingOpt.get().getType() == ChatType.PRIVATE
                    && participantRepository.existsByChatIdAndUserId(existingOpt.get().getId(), other)) {
                return Optional.of(toResponse(existingOpt.get(), me));
            }
        }
        return Optional.empty();
    }

    /**
     * Builds and persists a new {@link Chat} entity from the request.
     * PRIVATE chats are saved without a title; it is computed dynamically at read time.
     *
     * @param req       the creation request
     * @param type      the resolved chat type
     * @param createdBy the UUID of the user creating the chat
     * @return the saved {@link Chat} entity
     */
    private Chat buildAndSaveChat(ChatCreateRequest req, ChatType type, UUID createdBy) {
        String titleToSave = (type == ChatType.PRIVATE) ? null : req.getTitle();
        Chat chat = Chat.builder()
                .type(type)
                .title(titleToSave)
                .description(req.getDescription())
                .createdBy(createdBy)
                .build();
        return chatRepository.save(chat);
    }

    /**
     * Adds the chat creator as an {@link ParticipantRole#OWNER} participant.
     *
     * @param chatId the ID of the chat
     * @param userId the ID of the owner
     */
    private void addOwnerParticipant(UUID chatId, UUID userId) {
        participantRepository.save(Participant.builder()
                .chatId(chatId)
                .userId(userId)
                .role(ParticipantRole.OWNER)
                .build());
    }

    /**
     * Adds the peer user as a {@link ParticipantRole#MEMBER} in a PRIVATE chat, if not already present.
     * Also registers the peer in the {@code recipients} set for event notification.
     *
     * @param chatId     the ID of the chat
     * @param type       the chat type (only acts for {@link ChatType#PRIVATE})
     * @param otherId    the peer user's ID; may be {@code null}
     * @param recipients mutable set of user IDs to notify
     */
    private void addPrivateParticipantIfNeeded(UUID chatId, ChatType type, UUID otherId, Set<UUID> recipients) {
        if (type != ChatType.PRIVATE || otherId == null) return;
        if (!participantRepository.existsByChatIdAndUserId(chatId, otherId)) {
            participantRepository.save(Participant.builder()
                    .chatId(chatId)
                    .userId(otherId)
                    .role(ParticipantRole.MEMBER)
                    .build());
            recipients.add(otherId);
        }
    }

    /**
     * Adds the initial participant list (GROUP / CHANNEL creation) as {@link ParticipantRole#MEMBER}s.
     * Skips the owner, duplicates, and silently downgrades any attempt to assign {@link ParticipantRole#OWNER}.
     *
     * @param chatId       the ID of the chat
     * @param me           the owner's ID (already added; skipped here)
     * @param participants the requested participant list; may be {@code null} or empty
     * @param recipients   mutable set of user IDs to notify
     */
    private void addGroupParticipantsIfNeeded(UUID chatId, UUID me,
            List<ParticipantAddRequest> participants, Set<UUID> recipients) {
        if (participants == null || participants.isEmpty()) return;
        for (var pReq : participants) {
            if (pReq == null || pReq.getUserId() == null) continue;
            UUID uid = pReq.getUserId();
            if (uid.equals(me) || participantRepository.existsByChatIdAndUserId(chatId, uid)) continue;
            ParticipantRole role = pReq.getRole() == null ? ParticipantRole.MEMBER : pReq.getRole();
            if (role == ParticipantRole.OWNER) role = ParticipantRole.MEMBER; // disallow owner assignment via request
            participantRepository.save(Participant.builder()
                    .chatId(chatId)
                    .userId(uid)
                    .role(role)
                    .build());
            recipients.add(uid);
        }
    }

    /**
     * Broadcasts a {@code chat.created} WebSocket event to every participant.
     *
     * @param resp       the created chat response payload
     * @param recipients the set of user IDs to notify
     */
    private void notifyChatCreated(ChatResponse resp, Set<UUID> recipients) {
        var event = Map.of("type", "chat.created", "payload", resp);
        for (var uid : recipients) {
            messagingTemplate.convertAndSend("/user/" + uid + "/events", event);
        }
    }

    public ChatResponse get(UUID chatId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireMember(chatId, me);
        return toResponse(chatRepository.findById(chatId).orElseThrow(), me);
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
        var resp = toResponse(chat, me);
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

    public void requireMember(UUID chatId, UUID userId) {
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new IllegalArgumentException("Not a chat participant");
        }
    }

    private ChatResponse toResponse(Chat chat, UUID currentUserId) {
        String displayTitle = chat.getTitle();
        
        // Get all participants for this chat
        var participants = participantRepository.findByChatId(chat.getId());
        var participantUserIds = participants.stream()
                .map(Participant::getUserId)
                .toList();
        
        // Fetch all participant users in one query
        Map<UUID, User> usersMap = userRepository.findAllById(participantUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // Build participant info list
        List<ChatParticipantInfo> participantInfoList = participants.stream()
                .map(p -> {
                    User user = usersMap.get(p.getUserId());
                    return user != null ? ChatParticipantInfo.builder()
                            .id(user.getId())
                            .name(user.getUsername())
                            .avatar(user.getAvatarUrl())
                            .build() : null;
                })
                .filter(info -> info != null)
                .toList();
        
        // For PRIVATE chats, calculate title dynamically based on the other user
        if (chat.getType() == ChatType.PRIVATE) {
            // Find the other participant (not the current user)
            var otherUser = participants.stream()
                    .filter(p -> !p.getUserId().equals(currentUserId))
                    .findFirst();
            
            if (otherUser.isPresent()) {
                // Get the other user's name
                User user = usersMap.get(otherUser.get().getUserId());
                displayTitle = user != null ? user.getUsername() : "Unknown User";
            }
        }
        
        return ChatResponse.builder()
                .id(chat.getId())
                .type(chat.getType())
                .title(displayTitle)
                .description(chat.getDescription())
                .createdBy(chat.getCreatedBy())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .participants(participantInfoList)
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
