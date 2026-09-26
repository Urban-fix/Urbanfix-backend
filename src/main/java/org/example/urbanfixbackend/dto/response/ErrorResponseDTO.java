package org.example.urbanfixbackend.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details,
        Map<String, String> validationErrors
) {
    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null, null);
    }

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(timestamp, status, error, message, path, null, validationErrors);
    }
}
