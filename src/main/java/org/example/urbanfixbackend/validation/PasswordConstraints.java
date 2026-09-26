package org.example.urbanfixbackend.validation;

public final class PasswordConstraints {

    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,255}$";

    public static final String MESSAGE =
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número";

    private PasswordConstraints() {
    }
}
