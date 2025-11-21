package chat.jace.controller;

import chat.jace.domain.enums.PresenceStatus;
import chat.jace.service.UserPresenceService;
import chat.jace.service.PresenceCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/presence")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceService userPresenceService;
    private final PresenceCacheService presenceCacheService;

    @PostMapping("/check")
    public ResponseEntity<Map<UUID, PresenceStatus>> checkOnlineStatus(@RequestBody List<UUID> userIds) {
        Map<UUID, PresenceStatus> statusMap = presenceCacheService.getPresenceMap(userIds);
        return ResponseEntity.ok(statusMap);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPresence(@PathVariable UUID userId) {
        PresenceStatus status = presenceCacheService.getPresence(userId);
        var presence = userPresenceService.getUserPresence(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "status", status,
            "lastSeenAt", presence.getLastSeenAt()
        ));
    }
    
    @GetMapping("/online")
    public ResponseEntity<Map<UUID, PresenceStatus>> getAllOnlineUsers() {
        Map<UUID, PresenceStatus> onlineUsers = presenceCacheService.getAllOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }
}
