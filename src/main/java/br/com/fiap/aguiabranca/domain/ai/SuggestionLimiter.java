package br.com.fiap.aguiabranca.domain.ai;

import br.com.fiap.aguiabranca.domain.auth.RateLimitExceededException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Limite por usuario e de chamadas simultaneas, por instancia da API. */
@Component
public class SuggestionLimiter {

    private static final long WINDOW_NANOS = Duration.ofMinutes(1).toNanos();
    private static final int MAX_TRACKED_USERS = 10_000;
    private final SuggestionLimitProperties properties;
    private final LongSupplier nanoTime;
    private final Map<Long, Window> users = new HashMap<>();
    private int inFlight;
    private long lastCleanup;

    @Autowired
    public SuggestionLimiter(SuggestionLimitProperties properties) {
        this(properties, System::nanoTime);
    }

    SuggestionLimiter(SuggestionLimitProperties properties, LongSupplier nanoTime) {
        this.properties = properties;
        this.nanoTime = nanoTime;
        this.lastCleanup = nanoTime.getAsLong();
    }

    public <T> T execute(Long userId, Supplier<T> request) {
        acquire(userId);
        try {
            return request.get();
        } finally {
            release();
        }
    }

    private synchronized void acquire(Long userId) {
        long now = nanoTime.getAsLong();
        if (now - lastCleanup >= WINDOW_NANOS || users.size() >= MAX_TRACKED_USERS) {
            users.values().removeIf(window -> now - window.startedAt >= WINDOW_NANOS);
            lastCleanup = now;
        }
        Window window = users.get(userId);
        if (window != null && now - window.startedAt >= WINDOW_NANOS) {
            users.remove(userId);
            window = null;
        }
        if (window != null && window.requests >= properties.requestsPerMinute()) {
            long remaining = WINDOW_NANOS - (now - window.startedAt);
            throw new RateLimitExceededException(
                    "Limite de sugestoes atingido. Aguarde antes de tentar novamente.",
                    Math.max(1, (remaining + 999_999_999L) / 1_000_000_000L));
        }
        if (inFlight >= properties.maxConcurrent() || (window == null && users.size() >= MAX_TRACKED_USERS)) {
            throw new RateLimitExceededException("O assistente esta ocupado. Tente novamente em instantes.", 1);
        }
        if (window == null) {
            window = new Window(now);
            users.put(userId, window);
        }
        window.requests++;
        inFlight++;
    }

    private synchronized void release() {
        inFlight--;
    }

    private static final class Window {
        private final long startedAt;
        private int requests;

        private Window(long startedAt) {
            this.startedAt = startedAt;
        }
    }
}
