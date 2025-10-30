package chat.jace.security;

import chat.jace.domain.User;
import chat.jace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 🟢 Xác thực JWT
                String token = authHeader.substring(7);
                try {
                    String subject = jwtService.getSubject(token);
                    Optional<User> userOpt = userRepository.findById(UUID.fromString(subject));
                    if (userOpt.isPresent()) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                subject, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        accessor.setUser(auth);
                        log.info("✅ WebSocket CONNECT: Authenticated user from JWT: {}", subject);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Invalid JWT during WebSocket connect: {}", e.getMessage());
                }
            } else {
                // 🧪 Cho phép test qua header `user-id`
                String userIdHeader = accessor.getFirstNativeHeader("user-id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        UUID.fromString(userIdHeader); // validate UUID
                        var auth = new UsernamePasswordAuthenticationToken(
                                userIdHeader, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        accessor.setUser(auth);
                        log.info("✅ WebSocket CONNECT: Set user from header user-id={}", userIdHeader);
                    } catch (Exception e) {
                        log.warn("❌ Invalid user-id format: {}", e.getMessage());
                    }
                } else {
                    log.warn("⚠️ WebSocket CONNECT: No authentication header found");
                }
            }
        }

        return message;
    }
}
