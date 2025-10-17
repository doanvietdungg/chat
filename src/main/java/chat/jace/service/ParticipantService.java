package chat.jace.service;

import chat.jace.domain.Participant;
import chat.jace.domain.enums.ParticipantRole;
import chat.jace.dto.participant.ParticipantAddRequest;
import chat.jace.dto.participant.ParticipantResponse;
import chat.jace.dto.participant.ParticipantUpdateRequest;
import chat.jace.repository.ChatRepository;
import chat.jace.repository.ParticipantRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<ParticipantResponse> list(UUID chatId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireMember(chatId, me);
        return participantRepository.findByChatId(chatId)
                .stream().map(ParticipantService::toResponse).toList();
    }

    @Transactional
    public ParticipantResponse add(UUID chatId, ParticipantAddRequest req) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireAdmin(chatId, me);
        chatRepository.findById(chatId).orElseThrow();
        if (participantRepository.findByChatIdAndUserId(chatId, req.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User already a participant");
        }
        Participant p = Participant.builder()
                .chatId(chatId)
                .userId(req.getUserId())
                .role(req.getRole() == null ? ParticipantRole.MEMBER : req.getRole())
                .build();
        p = participantRepository.save(p);
        var resp = toResponse(p);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/events",
                java.util.Map.of("type", "participant.added", "payload", resp));
        return resp;
    }

    @Transactional
    public ParticipantResponse updateRole(UUID chatId, UUID targetUserId, ParticipantUpdateRequest req) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        requireOwner(chatId, me); // only owner can change roles
        Participant target = participantRepository.findByChatIdAndUserId(chatId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        if (target.getRole() == ParticipantRole.OWNER) {
            throw new IllegalArgumentException("Cannot change owner role");
        }
        if (req.getRole() == ParticipantRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role");
        }
        target.setRole(req.getRole());
        target = participantRepository.save(target);
        var resp = toResponse(target);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/events",
                java.util.Map.of("type", "participant.updated", "payload", resp));
        return resp;
    }

    @Transactional
    public void remove(UUID chatId, UUID targetUserId) {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        Participant mePart = participantRepository.findByChatIdAndUserId(chatId, me)
                .orElseThrow(() -> new IllegalArgumentException("Not a participant"));
        Participant target = participantRepository.findByChatIdAndUserId(chatId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        if (target.getRole() == ParticipantRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove owner");
        }
        // Admins can remove members; owner can remove anyone except owner
        if (mePart.getRole() == ParticipantRole.ADMIN && target.getRole() != ParticipantRole.MEMBER) {
            throw new IllegalArgumentException("Admins can only remove members");
        }
        if (mePart.getRole() == ParticipantRole.MEMBER) {
            throw new IllegalArgumentException("Insufficient permissions");
        }
        participantRepository.deleteById(new Participant.ParticipantId(chatId, targetUserId));
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/events",
                java.util.Map.of("type", "participant.removed", "payload", java.util.Map.of("userId", targetUserId.toString())));
    }

    private void requireMember(UUID chatId, UUID userId) {
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new IllegalArgumentException("Not a chat participant");
        }
    }

    private void requireAdmin(UUID chatId, UUID userId) {
        Participant p = participantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Not a chat participant"));
        if (p.getRole() == ParticipantRole.MEMBER) {
            throw new IllegalArgumentException("Admin or owner required");
        }
    }

    private void requireOwner(UUID chatId, UUID userId) {
        Participant p = participantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Not a chat participant"));
        if (p.getRole() != ParticipantRole.OWNER) {
            throw new IllegalArgumentException("Owner required");
        }
    }

    private static ParticipantResponse toResponse(Participant p) {
        return ParticipantResponse.builder()
                .chatId(p.getChatId())
                .userId(p.getUserId())
                .role(p.getRole())
                .joinedAt(p.getJoinedAt())
                .build();
    }
}
