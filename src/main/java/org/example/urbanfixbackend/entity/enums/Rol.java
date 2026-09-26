package org.example.urbanfixbackend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Rol {
    CIUDADANO,
    ADMIN_MUNICIPAL,
    TECNICO,
    SUPERVISOR,
    OPERADOR,
    AUDITOR;

    @JsonCreator
    public static Rol fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Rol.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + value + ". Valores válidos: " + java.util.Arrays.toString(values()));
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
