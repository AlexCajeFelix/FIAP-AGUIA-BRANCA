package br.com.fiap.aguiabranca.domain.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        rateLimiter.checkRateLimit(clientIp, request.email());

        TokenResponse tokenResponse = authService.login(request);
        rateLimiter.resetEmailLimit(request.email());
        return ResponseEntity.ok(tokenResponse);
    }
}
