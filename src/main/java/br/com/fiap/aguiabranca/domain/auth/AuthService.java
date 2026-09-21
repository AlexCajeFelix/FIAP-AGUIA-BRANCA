package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login e rotacao de refresh token.
 *
 * Nao ha @Transactional aqui de proposito. O que a transacao protegia era a leitura seguida de
 * revogacao do token — e isso agora acontece numa unica operacao atomica no servidor
 * ({@link RefreshTokenRevocation#claimIfActive}). Abrir transacao do Mongo em volta traria de
 * volta o problema que ela resolvia, so com outro nome: duas requisicoes concorrentes sobre o
 * mesmo documento dariam WriteConflict em vez de cair no caminho de reuso.
 */
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
     */
    public TokenResponse refresh(RefreshRequest request) {
        String hash = RefreshTokens.hash(request.refreshToken());
        Instant now = Instant.now();

        RefreshToken current = refreshTokens.claimIfActive(hash, now)
                .orElseThrow(() -> reuseOrUnknown(hash, now));

        // Expirado ja saiu revogado do claim: nao da para trocar, mas tambem nao e roubo.
        if (current.isExpired(now)) {
            throw new InvalidRefreshTokenException();
        }

        User user = users.findById(current.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);
        return issuePair(user, current.getFamilyId());
    }

    public void logout(RefreshRequest request) {
        String hash = RefreshTokens.hash(request.refreshToken());
        if (refreshTokens.claimIfActive(hash, Instant.now()).isEmpty()
                && refreshTokens.findByTokenHash(hash).isEmpty()) {
            throw new InvalidRefreshTokenException();
        }
    }

    /**
     * Claim vazio tem duas causas: o token nunca existiu, ou ja estava revogado. So a segunda
     * e reuso, e so ela derruba a familia.
     */
    private InvalidRefreshTokenException reuseOrUnknown(String hash, Instant now) {
        refreshTokens.findByTokenHash(hash)
                .ifPresent(reused -> refreshTokens.revokeFamily(reused.getFamilyId(), now));
        return new InvalidRefreshTokenException();
    }

    private TokenResponse issuePair(User user, UUID familyId) {
        String rawRefresh = RefreshTokens.generateRaw();
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshExpiration());
        refreshTokens.save(new RefreshToken(RefreshTokens.hash(rawRefresh), user.getId(), familyId, expiresAt));
        return new TokenResponse(jwtService.generate(user), rawRefresh, jwtService.expiresInSeconds(),
                user.getRole());
    }
}
