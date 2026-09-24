package org.example.urbanfixbackend.exception;

public class CategoriaNotFoundException extends RuntimeException {
    public CategoriaNotFoundException(Long id) {
        super("Categoría no encontrada con ID: " + id);
    }

    public CategoriaNotFoundException(String nombre) {
        super("Categoría no encontrada con nombre: " + nombre);
    }
}
