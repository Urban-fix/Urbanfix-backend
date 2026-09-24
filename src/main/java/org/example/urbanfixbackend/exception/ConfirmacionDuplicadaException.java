package org.example.urbanfixbackend.exception;

public class ConfirmacionDuplicadaException extends RuntimeException {
    public ConfirmacionDuplicadaException(Long reporteId, Long usuarioId) {
        super("El usuario ya ha confirmado este reporte - ReporteID: " + reporteId + ", UsuarioID: " + usuarioId);
    }
}
