package br.com.fiap.aguiabranca.domain.ai;

import br.com.fiap.aguiabranca.domain.auth.RateLimitExceededException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuggestionLimiterTest {

    @Test
    void shouldLimitByUserAndRecoverAfterWindowWithoutSleeping() {
        AtomicLong time = new AtomicLong();
        var limiter = new SuggestionLimiter(new SuggestionLimitProperties(2, 1), time::get);
        limiter.execute(1L, () -> "primeira");
        limiter.execute(1L, () -> "segunda");
        time.set(Duration.ofSeconds(10).toNanos());
        assertThatThrownBy(() -> limiter.execute(1L, () -> "terceira"))
                .isInstanceOfSatisfying(RateLimitExceededException.class,
                        ex -> assertThat(ex.getRetryAfterSeconds()).isEqualTo(50));
        assertThat(limiter.execute(2L, () -> "outro usuario")).isEqualTo("outro usuario");
        time.set(Duration.ofMinutes(1).toNanos());
        assertThat(limiter.execute(1L, () -> "nova janela")).isEqualTo("nova janela");
    }

    @Test
    void shouldReleaseCapacityEvenWhenProviderFails() {
        var limiter = new SuggestionLimiter(new SuggestionLimitProperties(2, 1));
        assertThatThrownBy(() -> limiter.execute(1L, () -> {
            throw new SuggestionUnavailableException("falhou");
        })).isInstanceOf(SuggestionUnavailableException.class);
        assertThat(limiter.execute(2L, () -> "recuperou")).isEqualTo("recuperou");
    }

    @Test
    void shouldRejectConcurrentCallsWithoutSpendingUserQuota() throws Exception {
        var limiter = new SuggestionLimiter(new SuggestionLimitProperties(1, 1));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> limiter.execute(1L, () -> {
                started.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                return "ok";
            }));
            try {
                assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> limiter.execute(2L, () -> "nao deveria chamar"))
                        .isInstanceOf(RateLimitExceededException.class);
            } finally {
                release.countDown();
            }
            assertThat(first.get(3, TimeUnit.SECONDS)).isEqualTo("ok");
            assertThat(limiter.execute(2L, () -> "quota preservada")).isEqualTo("quota preservada");
        }
    }

    @Test
    void shouldExpireOldUsersInsteadOfGrowingForever() {
        AtomicLong time = new AtomicLong();
        var limiter = new SuggestionLimiter(new SuggestionLimitProperties(1, 1), time::get);
        for (long user = 0; user < 10_000; user++) {
            limiter.execute(user, () -> "ok");
        }
        assertThatThrownBy(() -> limiter.execute(10_001L, () -> "lotado"))
                .isInstanceOf(RateLimitExceededException.class);
        time.set(Duration.ofMinutes(1).toNanos());
        assertThat(limiter.execute(10_001L, () -> "limpou")).isEqualTo("limpou");
    }
}
