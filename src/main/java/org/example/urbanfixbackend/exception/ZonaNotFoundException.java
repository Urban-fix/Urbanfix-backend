package org.example.urbanfixbackend.exception;

public class ZonaNotFoundException extends RuntimeException {
    public ZonaNotFoundException(Long id) {
        super("Zona no encontrada con ID: " + id);
    }

    public ZonaNotFoundException(String nombre) {
        super("Zona no encontrada con nombre: " + nombre);
    }
}
