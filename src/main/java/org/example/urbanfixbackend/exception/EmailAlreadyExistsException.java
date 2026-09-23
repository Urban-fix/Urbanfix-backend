package org.example.urbanfixbackend.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String email) {
        super("El email ya está registrado: " + email);
    }
}
