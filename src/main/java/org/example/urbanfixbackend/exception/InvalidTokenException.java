package org.example.urbanfixbackend.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        super("Token inválido o mal formado");
    }
}
