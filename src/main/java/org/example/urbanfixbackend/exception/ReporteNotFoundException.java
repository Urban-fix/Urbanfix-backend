package org.example.urbanfixbackend.exception;

public class ReporteNotFoundException extends RuntimeException {
    public ReporteNotFoundException(String message) {
        super(message);
    }

    public ReporteNotFoundException(Long id) {
        super("Reporte no encontrado con ID: " + id);
    }
}
