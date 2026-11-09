package org.example.urbanfixbackend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.mail.host", matchIfMissing = true, havingValue = "disabled")
@Slf4j
public class NoOpEmailServiceImpl implements EmailService {

    @Override
    public void sendWelcomeEmail(String to, String nombre) {
        log.info("[EMAIL DISABLED] Welcome email would be sent to: {} for user: {}", to, nombre);
    }

    @Override
    public void sendEstadoCambiadoEmail(String to, String reporteTitulo, String estadoAnterior, String estadoNuevo) {
        log.info("[EMAIL DISABLED] State change email would be sent to: {} for report: {} ({} -> {})", 
                to, reporteTitulo, estadoAnterior, estadoNuevo);
    }

    @Override
    public void sendPasswordResetEmail(String to, String nombre, String resetCode) {
        log.info("[EMAIL DISABLED] Password reset email would be sent to: {} for user: {} with code: {}", 
                to, nombre, resetCode);
    }
}
