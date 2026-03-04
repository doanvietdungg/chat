package chat.jace.controller;

import chat.jace.dto.auth.AuthResponse;
import chat.jace.dto.auth.LoginRequest;
import chat.jace.dto.auth.RefreshTokenRequest;
import chat.jace.dto.auth.RegisterRequest;
import chat.jace.dto.auth.TokenResponse;
import chat.jace.dto.common.ResponseFactory;
import chat.jace.service.AuthService;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseFactory.created(response, "Đăng ký thành công");
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseFactory.success(response, "Đăng nhập thành công");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request.getRefreshToken());
        return ResponseFactory.success(response, "Làm mới token thành công");
    }
}
