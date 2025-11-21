package chat.jace.events;

import chat.jace.domain.enums.PresenceStatus;
import chat.jace.service.PresenceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceCacheService presenceCacheService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserIdFromHeaders(headerAccessor);
        
        if (userId != null) {
            log.info("WebSocket connected - User: {}, Session: {}", userId, headerAccessor.getSessionId());
            presenceCacheService.updatePresence(UUID.fromString(userId), PresenceStatus.ONLINE);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserIdFromHeaders(headerAccessor);
        
        if (userId != null) {
            log.info("WebSocket disconnected - User: {}, Session: {}", userId, headerAccessor.getSessionId());
            presenceCacheService.updatePresence(UUID.fromString(userId), PresenceStatus.OFFLINE);
        }
    }

    private String getUserIdFromHeaders(StompHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() != null) {
            return headerAccessor.getUser().getName();
        }
        return null;
    }
}
