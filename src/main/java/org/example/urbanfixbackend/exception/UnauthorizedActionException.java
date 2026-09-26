package org.example.urbanfixbackend.exception;

public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException() {
        super("No tienes permisos para realizar esta acción");
    }
}
