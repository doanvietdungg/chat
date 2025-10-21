package chat.jace.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static UUID currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthenticated");
        return UUID.fromString(auth.getName());
    }
    
    public static UUID getCurrentUserId() {
        return currentUserIdOrThrow();
    }
    
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthenticated");
        return auth.getName();
    }
    
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getName() != null;
    }
}
