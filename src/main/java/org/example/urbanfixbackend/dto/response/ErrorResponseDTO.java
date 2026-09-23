package org.example.urbanfixbackend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}
