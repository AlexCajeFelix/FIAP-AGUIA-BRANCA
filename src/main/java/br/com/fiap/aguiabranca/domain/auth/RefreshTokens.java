package br.com.fiap.aguiabranca.domain.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh token opaco. Nao e JWT: sem contraparte no banco nao ha como revogar.
 * O valor em claro so viaja na resposta; o banco guarda o SHA-256.
 */
final class RefreshTokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RAW_BYTES = 32;

    private RefreshTokens() {
    }

    static String generateRaw() {
        byte[] bytes = new byte[RAW_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 e obrigatorio na JVM", ex);
        }
    }
}
