package br.com.fiap.aguiabranca.domain.auth;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Gestor de rate limit por IP e E-mail baseado em buckets em memoria
 * (Bucket4j).
 *
 * NOTA DE ARQUITETURA (#9):
 * Esta implementacao em memoria atende instancias unicas. Para deploys com
 * escalamento
 * horizontal, a persistencia de estado deve ser migrada para Redis (ex:
 * bucket4j-redis).
 */
@Service
public class LoginRateLimiter {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> emailBuckets = new ConcurrentHashMap<>();

    public LoginRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
    }

    public void checkRateLimit(String ip, String email) {
        if (!properties.enabled()) {
            return;
        }

        String safeIp = (ip == null || ip.isBlank()) ? "unknown-ip" : ip.trim();
        String safeEmail = (email == null) ? "" : email.trim().toLowerCase(Locale.ROOT);

        Bucket ipBucket = ipBuckets.computeIfAbsent(safeIp, k -> createIpBucket());
        ConsumptionProbe ipProbe = ipBucket.tryConsumeAndReturnRemaining(1);
        if (!ipProbe.isConsumed()) {
            long seconds = calculateRetryAfterSeconds(ipProbe.getNanosToWaitForRefill());
            throw new RateLimitExceededException(
                    "Limite de tentativas por IP excedido. Tente novamente mais tarde.", seconds);
        }

        if (!safeEmail.isBlank()) {
            Bucket emailBucket = emailBuckets.computeIfAbsent(safeEmail, k -> createEmailBucket());
            ConsumptionProbe emailProbe = emailBucket.tryConsumeAndReturnRemaining(1);
            if (!emailProbe.isConsumed()) {
                long seconds = calculateRetryAfterSeconds(emailProbe.getNanosToWaitForRefill());
                throw new RateLimitExceededException(
                        "Limite de tentativas por e-mail excedido. Tente novamente mais tarde.", seconds);
            }
        }
    }

    public void resetEmailLimit(String email) {
        if (email != null && !email.isBlank()) {
            String safeEmail = email.trim().toLowerCase(Locale.ROOT);
            emailBuckets.remove(safeEmail);
        }
    }

    public void clearAllLimits() {
        ipBuckets.clear();
        emailBuckets.clear();
    }

    private Bucket createIpBucket() {
        RateLimitProperties.LimitRule rule = properties.ip();
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity())
                .refillGreedy(rule.capacity(), Duration.ofMinutes(rule.durationMinutes()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createEmailBucket() {
        RateLimitProperties.LimitRule rule = properties.email();
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity())
                .refillGreedy(rule.capacity(), Duration.ofMinutes(rule.durationMinutes()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private long calculateRetryAfterSeconds(long nanosToWait) {
        if (nanosToWait <= 0) {
            return 1;
        }
        return (long) Math.ceil(nanosToWait / 1_000_000_000.0);
    }
}
