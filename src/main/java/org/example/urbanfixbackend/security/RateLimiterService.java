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
        private int maxAttempts;
        private int windowMinutes;

        RateLimitInfo(int maxAttempts, int windowMinutes) {
            this.attempts = 0;
            this.windowStart = LocalDateTime.now();
            this.maxAttempts = maxAttempts;
            this.windowMinutes = windowMinutes;
        }
    }

    public boolean tryConsume(String key) {
        return tryConsume(key, 3, 2); // Valores por defecto: 3 intentos en 2 minutos
    }

    public boolean tryConsume(String key, int maxAttempts, int windowMinutes) {
        RateLimitInfo info = rateLimits.computeIfAbsent(key, k -> new RateLimitInfo(maxAttempts, windowMinutes));
        LocalDateTime now = LocalDateTime.now();

        // Reset si la ventana de tiempo ha pasado
        if (info.windowStart.plusMinutes(info.windowMinutes).isBefore(now)) {
            info.attempts = 0;
            info.windowStart = now;
        }

        // Verificar si excede el límite de intentos
        if (info.attempts >= info.maxAttempts) {
            return false;
        }

        info.attempts++;
        return true;
    }

    public void resetBucket(String key) {
        rateLimits.remove(key);
    }
}
