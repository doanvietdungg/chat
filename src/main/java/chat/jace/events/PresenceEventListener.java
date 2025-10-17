package chat.jace.events;

import chat.jace.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private final ParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        Principal p = event.getUser();
        if (p == null || p.getName() == null) return;
        UUID userId = UUID.fromString(p.getName());
        participantRepository.findByUserId(userId).forEach(part ->
            messagingTemplate.convertAndSend("/topic/chats/" + part.getChatId() + "/events",
                Map.of("type", "user.online", "payload", Map.of("userId", userId.toString())))
        );
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal p = event.getUser();
        if (p == null || p.getName() == null) return;
        UUID userId = UUID.fromString(p.getName());
        participantRepository.findByUserId(userId).forEach(part ->
            messagingTemplate.convertAndSend("/topic/chats/" + part.getChatId() + "/events",
                Map.of("type", "user.offline", "payload", Map.of("userId", userId.toString())))
        );
    }
}
