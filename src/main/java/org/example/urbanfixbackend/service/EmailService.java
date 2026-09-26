package org.example.urbanfixbackend.service;

public interface EmailService {

    void sendWelcomeEmail(String to, String nombre);

    void sendEstadoCambiadoEmail(String to, String reporteTitulo, String estadoAnterior, String estadoNuevo);

    void sendPasswordResetEmail(String to, String nombre, String resetLink);
}
