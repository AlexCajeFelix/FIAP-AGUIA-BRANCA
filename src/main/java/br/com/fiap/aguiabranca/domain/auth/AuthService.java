package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokens;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenRepository refreshTokens, JwtProperties jwtProperties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("credenciais invalidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("credenciais invalidas");
        }

        return issuePair(user, UUID.randomUUID());
    }

    /**
     * Troca um refresh valido por um par novo e invalida o usado.
     * Reuso de um token ja rotacionado revoga a familia inteira — sinal de roubo.
     *
     * InvalidRefreshTokenException nao reverte a transacao: a revogacao da familia
     * precisa persistir, senao o token recem-rotacionado continua valido.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken current = lookup(request.refreshToken());
        Instant now = Instant.now();

        if (current.isRevoked()) {
            refreshTokens.findByFamilyId(current.getFamilyId())
                    .forEach(token -> token.revoke(now));
            throw new InvalidRefreshTokenException();
        }

        if (current.isExpired(now)) {
            current.revoke(now);
            throw new InvalidRefreshTokenException();
        }

        current.revoke(now);
        User user = current.getUser();
        UUID familyId = current.getFamilyId();
        return issuePair(user, familyId);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public void logout(RefreshRequest request) {
        RefreshToken current = lookup(request.refreshToken());
        current.revoke(Instant.now());
    }

    private RefreshToken lookup(String raw) {
        return refreshTokens.findByTokenHashForUpdate(RefreshTokens.hash(raw))
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private TokenResponse issuePair(User user, UUID familyId) {
        String rawRefresh = RefreshTokens.generateRaw();
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshExpiration());
        refreshTokens.save(new RefreshToken(RefreshTokens.hash(rawRefresh), user, familyId, expiresAt));
        return new TokenResponse(jwtService.generate(user), rawRefresh, jwtService.expiresInSeconds(),
                user.getRole());
    }
}
