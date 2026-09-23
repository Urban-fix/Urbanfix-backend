package org.example.urbanfixbackend.exception;

public class DatabaseConstraintViolationException extends RuntimeException {
    public DatabaseConstraintViolationException(String message) {
        super(message);
    }

    public DatabaseConstraintViolationException(String constraint, String reason) {
        super("Violación de constraint '" + constraint + "': " + reason);
    }
}
