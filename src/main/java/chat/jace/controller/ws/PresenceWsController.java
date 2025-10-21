package chat.jace.controller.ws;

import chat.jace.domain.enums.PresenceStatus;
import chat.jace.dto.ws.PresencePayload;
import chat.jace.service.UserPresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PresenceWsController {

    private final UserPresenceService userPresenceService;

    @MessageMapping("/presence.update")
    public void updatePresence(@Valid PresencePayload payload, Principal principal) {
        String userId = principal != null ? principal.getName() : null;
        if (userId == null) return;
        
        userPresenceService.updateUserPresence(UUID.fromString(userId), payload.getStatus());
    }

    @MessageMapping("/presence.heartbeat")
    public void heartbeat(Principal principal) {
        String userId = principal != null ? principal.getName() : null;
        if (userId == null) return;
        
        // Update last seen and ensure user is online
        userPresenceService.updateLastSeen(UUID.fromString(userId));
        userPresenceService.updateUserPresence(UUID.fromString(userId), PresenceStatus.ONLINE);
    }
}