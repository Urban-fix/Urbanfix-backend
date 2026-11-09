package org.example.urbanfixbackend.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();

    private static class RateLimitInfo {
        private int attempts;
        private LocalDateTime windowStart;

        RateLimitInfo() {
            this.attempts = 0;
            this.windowStart = LocalDateTime.now();
        }
    }

    public boolean tryConsume(String key) {
        RateLimitInfo info = rateLimits.computeIfAbsent(key, k -> new RateLimitInfo());
        LocalDateTime now = LocalDateTime.now();

        // Reset si la ventana de 2 minutos ha pasado
        if (info.windowStart.plusMinutes(2).isBefore(now)) {
            info.attempts = 0;
            info.windowStart = now;
        }

        // Verificar si excede el límite de 3 intentos
        if (info.attempts >= 3) {
            return false;
        }

        info.attempts++;
        return true;
    }

    public void resetBucket(String key) {
        rateLimits.remove(key);
    }
}
