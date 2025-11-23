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
import org.springframework.security.core.context.SecurityContextHolder;
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

        // Xử lý CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateAndSetUser(accessor);
            log.info("✅ CONNECT authenticated: {}", accessor.getUser() != null ? accessor.getUser().getName() : "anonymous");
        }

        // ⚠️ QUAN TRỌNG: Xử lý SEND - set SecurityContext
        else if (StompCommand.SEND.equals(accessor.getCommand())) {
            // Bước 1: Lấy user từ session (đã set ở CONNECT)
            UsernamePasswordAuthenticationToken auth = null;

            if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
                auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
                log.debug("✅ SEND - Got user from session: {}", auth.getName());
            } else {
                // Bước 2: Fallback - authenticate từ headers nếu không có user
                log.warn("⚠️ SEND - No user in session, trying to authenticate from headers");
                authenticateAndSetUser(accessor);
                if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
                    auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
                }
            }

            // Bước 3: Set vào SecurityContext (BẮT BUỘC)
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("✅ SEND - Set SecurityContext for user: {}", auth.getName());
            } else {
                log.error("❌ SEND - Cannot authenticate user, SecurityContext will be null!");
                // Có thể throw exception ở đây nếu muốn bắt buộc authentication
                // throw new IllegalStateException("Authentication required for WebSocket messages");
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // Clear SecurityContext sau khi xử lý xong
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
            SecurityContextHolder.clearContext();
            log.debug("🧹 Cleared SecurityContext after SEND");
        }
    }

    private void authenticateAndSetUser(StompHeaderAccessor accessor) {
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
                    log.info("✅ Authenticated user from JWT: {}", subject);
                } else {
                    log.warn("⚠️ User not found for subject: {}", subject);
                }
            } catch (Exception e) {
                log.error("❌ JWT authentication failed: {}", e.getMessage());
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
                    log.info("✅ Set user from header user-id={}", userIdHeader);
                } catch (Exception e) {
                    log.warn("❌ Invalid user-id format: {}", e.getMessage());
                }
            } else {
                log.warn("⚠️ No authentication header found (neither Bearer token nor user-id)");
            }
        }
    }
}