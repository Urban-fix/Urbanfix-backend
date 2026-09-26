package org.example.urbanfixbackend.exception;

import org.example.urbanfixbackend.entity.enums.EstadoReporte;

public class EstadoTransicionInvalidaException extends RuntimeException {
    public EstadoTransicionInvalidaException(String message) {
        super(message);
    }

    public EstadoTransicionInvalidaException(EstadoReporte estadoActual, EstadoReporte estadoNuevo) {
        super("Transición de estado inválida: de " + estadoActual + " a " + estadoNuevo);
    }
}
