package org.example.urbanfixbackend.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException() {
        super("El token ha expirado");
    }
}
