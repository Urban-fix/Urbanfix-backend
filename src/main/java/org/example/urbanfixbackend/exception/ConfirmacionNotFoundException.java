package org.example.urbanfixbackend.exception;

public class ConfirmacionNotFoundException extends RuntimeException {
    public ConfirmacionNotFoundException(Long reporteId, Long usuarioId) {
        super("Confirmación no encontrada - ReporteID: " + reporteId + ", UsuarioID: " + usuarioId);
    }
}
