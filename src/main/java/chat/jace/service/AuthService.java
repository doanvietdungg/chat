package chat.jace.service;

import chat.jace.domain.User;
import chat.jace.dto.auth.AuthResponse;
import chat.jace.dto.auth.LoginRequest;
import chat.jace.dto.auth.RegisterRequest;
import chat.jace.dto.auth.TokenResponse;
import chat.jace.dto.user.UserResponse;
import chat.jace.repository.UserRepository;
import chat.jace.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists");
        });

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();
        user = userRepository.save(user);

        String access = jwtService.generateAccessToken(user.getId().toString(), Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        ));
        String refresh = jwtService.generateRefreshToken(user.getId().toString());
        
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        // principal username is userId as set in CustomUserDetailsService
        String userId = auth.getName();
        
        // Get user details
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String access = jwtService.generateAccessToken(userId, Map.of());
        String refresh = jwtService.generateRefreshToken(userId);
        
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    public TokenResponse refresh(String refreshToken) {
        String subject = jwtService.getSubject(refreshToken);
        // could validate exp and existence; jwt parser already checks signature/exp
        String access = jwtService.generateAccessToken(subject, Map.of());
        String newRefresh = jwtService.generateRefreshToken(subject);
        return new TokenResponse(access, newRefresh, "Bearer");
    }
    
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
