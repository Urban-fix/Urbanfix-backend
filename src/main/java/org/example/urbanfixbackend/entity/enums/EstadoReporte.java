package org.example.urbanfixbackend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoReporte {
    REPORTADO,
    EN_PROCESO,
    RESUELTO,
    RECHAZADO;

    @JsonCreator
    public static EstadoReporte fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return EstadoReporte.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + value + ". Valores válidos: " + java.util.Arrays.toString(values()));
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
