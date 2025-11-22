package chat.jace.controller;

import chat.jace.domain.enums.PresenceStatus;
import chat.jace.dto.common.ResponseFactory;
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

    @PostMapping("/status")
    public ResponseEntity<?> checkOnlineStatus(@RequestBody List<UUID> userIds) {
        Map<UUID, PresenceStatus> statusMap = presenceCacheService.getPresenceMap(userIds);
        return ResponseFactory.success(statusMap, "Lấy trạng thái người dùng thành công");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserPresence(@PathVariable UUID userId) {
        PresenceStatus status = presenceCacheService.getPresence(userId);
        var presence = userPresenceService.getUserPresence(userId);
        Map<String, Object> data = Map.of(
            "userId", userId,
            "status", status,
            "lastSeenAt", presence.getLastSeenAt()
        );
        return ResponseFactory.success(data, "Lấy thông tin trạng thái thành công");
    }
    
    @GetMapping("/online")
    public ResponseEntity<?> getAllOnlineUsers() {
        Map<UUID, PresenceStatus> onlineUsers = presenceCacheService.getAllOnlineUsers();
        return ResponseFactory.success(onlineUsers, "Lấy danh sách người dùng online thành công");
    }
}
