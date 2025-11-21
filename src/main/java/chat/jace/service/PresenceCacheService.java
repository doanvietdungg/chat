package chat.jace.service;

import chat.jace.domain.enums.PresenceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceCacheService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserPresenceService userPresenceService;
    
    // Cache: userId -> PresenceInfo
    private final Map<UUID, PresenceInfo> presenceCache = new ConcurrentHashMap<>();
    
    public void updatePresence(UUID userId, PresenceStatus status) {
        OffsetDateTime now = OffsetDateTime.now();
        
        PresenceInfo oldInfo = presenceCache.get(userId);
        PresenceStatus oldStatus = oldInfo != null ? oldInfo.status : null;
        
        // Update cache
        presenceCache.put(userId, new PresenceInfo(status, now));
        
        // Persist to database async
        userPresenceService.updateUserPresence(userId, status);
        
        // Broadcast if status changed
        if (!status.equals(oldStatus)) {
            log.info("User {} presence changed from {} to {}", userId, oldStatus, status);
            broadcastPresenceChange(userId, status, now);
        }
    }
    
    public Map<UUID, PresenceStatus> getPresenceMap(List<UUID> userIds) {
        Map<UUID, PresenceStatus> result = new HashMap<>();
        
        for (UUID userId : userIds) {
            PresenceInfo info = presenceCache.get(userId);
            if (info != null) {
                result.put(userId, info.status);
            } else {
                // Fallback to DB if not in cache
                result.put(userId, PresenceStatus.OFFLINE);
            }
        }
        
        return result;
    }
    
    public PresenceStatus getPresence(UUID userId) {
        PresenceInfo info = presenceCache.get(userId);
        return info != null ? info.status : PresenceStatus.OFFLINE;
    }
    
    public Map<UUID, PresenceStatus> getAllOnlineUsers() {
        Map<UUID, PresenceStatus> result = new HashMap<>();
        presenceCache.forEach((userId, info) -> {
            if (info.status == PresenceStatus.ONLINE) {
                result.put(userId, info.status);
            }
        });
        return result;
    }
    
    // Auto cleanup offline users every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void cleanupStalePresence() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(10);
        List<UUID> toRemove = new ArrayList<>();
        
        presenceCache.forEach((userId, info) -> {
            if (info.lastUpdate.isBefore(threshold) && info.status == PresenceStatus.ONLINE) {
                toRemove.add(userId);
            }
        });
        
        for (UUID userId : toRemove) {
            log.info("Auto setting user {} to OFFLINE due to inactivity", userId);
            presenceCache.put(userId, new PresenceInfo(PresenceStatus.OFFLINE, OffsetDateTime.now()));
            userPresenceService.updateUserPresence(userId, PresenceStatus.OFFLINE);
            broadcastPresenceChange(userId, PresenceStatus.OFFLINE, OffsetDateTime.now());
        }
    }
    
    private void broadcastPresenceChange(UUID userId, PresenceStatus status, OffsetDateTime timestamp) {
        Map<String, Object> event = Map.of(
            "type", "presence.changed",
            "userId", userId,
            "status", status,
            "timestamp", timestamp
        );
        
        messagingTemplate.convertAndSend("/topic/presence", event);
    }
    
    private static class PresenceInfo {
        PresenceStatus status;
        OffsetDateTime lastUpdate;
        
        PresenceInfo(PresenceStatus status, OffsetDateTime lastUpdate) {
            this.status = status;
            this.lastUpdate = lastUpdate;
        }
    }
}
