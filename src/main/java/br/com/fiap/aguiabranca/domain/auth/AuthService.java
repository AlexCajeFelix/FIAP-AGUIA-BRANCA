package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("credenciais invalidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("credenciais invalidas");
        }

        return new TokenResponse(jwtService.generate(user), jwtService.expiresInSeconds(), user.getRole());
    }
}
