package org.example.urbanfixbackend.service;

public interface EmailService {

    void sendWelcomeEmail(String to, String nombre);

    void sendEstadoCambiadoEmail(String to, String reporteTitulo, String estadoNuevo);
}
