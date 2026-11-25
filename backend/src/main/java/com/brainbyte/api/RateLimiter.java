package com.brainbyte.api;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class RateLimiter {
    private final ConcurrentMap<String, Deque<Long>> requests = new ConcurrentHashMap<>();

    public void check(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long cutoff = now - window.toMillis();
        Deque<Long> timestamps = requests.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= limit) {
                long retryAfterSeconds = Math.max(1, (timestamps.peekFirst() + window.toMillis() - now + 999) / 1000);
                throw new RateLimitException(
                        retryAfterSeconds,
                        "Too many requests. Please try again later.");
            }
            timestamps.addLast(now);
        }
    }

    @Scheduled(fixedDelay = 900_000)
    void removeExpiredBuckets() {
        long cutoff = System.currentTimeMillis() - Duration.ofMinutes(15).toMillis();
        requests.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                    timestamps.removeFirst();
                }
                return timestamps.isEmpty();
            }
        });
    }

    public static class RateLimitException extends ApiException {
        private final long retryAfterSeconds;

        public RateLimitException(long retryAfterSeconds, String message) {
            super(HttpStatus.TOO_MANY_REQUESTS, message);
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public long retryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}
