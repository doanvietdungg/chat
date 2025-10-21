package chat.jace.service;

import chat.jace.domain.UserPresence;
import chat.jace.domain.enums.PresenceStatus;
import chat.jace.repository.UserPresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final UserPresenceRepository userPresenceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void updateUserPresence(UUID userId, PresenceStatus status) {
        OffsetDateTime now = OffsetDateTime.now();
        
        UserPresence presence = userPresenceRepository.findByUserId(userId)
                .orElse(UserPresence.builder()
                        .userId(userId)
                        .status(status)
                        .lastSeenAt(now)
                        .updatedAt(now)
                        .build());
        
        PresenceStatus oldStatus = presence.getStatus();
        presence.setStatus(status);
        presence.setUpdatedAt(now);
        
        if (status == PresenceStatus.ONLINE) {
            presence.setLastSeenAt(now);
        }
        
        userPresenceRepository.save(presence);
        
        // Broadcast presence change if status changed
        if (!oldStatus.equals(status)) {
            broadcastPresenceChange(userId, status, now);
        }
    }

    @Transactional
    public void updateLastSeen(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        userPresenceRepository.updateLastSeenByUserId(userId, now, now);
    }

    @Transactional(readOnly = true)
    public UserPresence getUserPresence(UUID userId) {
        return userPresenceRepository.findByUserId(userId)
                .orElse(UserPresence.builder()
                        .userId(userId)
                        .status(PresenceStatus.OFFLINE)
                        .lastSeenAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build());
    }

    private void broadcastPresenceChange(UUID userId, PresenceStatus status, OffsetDateTime timestamp) {
        Map<String, Object> presenceEvent = new HashMap<>();
        presenceEvent.put("userId", userId);
        presenceEvent.put("status", status);
        presenceEvent.put("timestamp", timestamp);
        presenceEvent.put("type", "presence.changed");
        
        // Broadcast to all users (they can filter based on their contacts)
        messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
    }
}