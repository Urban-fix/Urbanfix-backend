package org.example.urbanfixbackend.exception;

public class InvalidCoordinatesException extends RuntimeException {
    public InvalidCoordinatesException(String message) {
        super(message);
    }

    public InvalidCoordinatesException(Double latitud, Double longitud) {
        super("Coordenadas inválidas - Latitud: " + latitud + ", Longitud: " + longitud);
    }
}
