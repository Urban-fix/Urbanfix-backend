package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendWelcomeEmail(String to, String nombre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Bienvenido a UrbanFix");
            message.setText("Hola " + nombre + ",\n\n" +
                    "Gracias por registrarte en UrbanFix. " +
                    "Ahora puedes reportar problemas en tu comunidad y ayudar a mejorar tu ciudad.\n\n" +
                    "Saludos,\nEl equipo de UrbanFix");
            
            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a: {}", to, e);
        }
    }

    @Override
    public void sendEstadoCambiadoEmail(String to, String reporteTitulo, String estadoNuevo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Actualización de reporte: " + reporteTitulo);
            message.setText("Hola,\n\n" +
                    "El estado de tu reporte '" + reporteTitulo + "' ha sido actualizado a: " + estadoNuevo + ".\n\n" +
                    "Saludos,\nEl equipo de UrbanFix");
            
            mailSender.send(message);
            log.info("Email de cambio de estado enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de cambio de estado a: {}", to, e);
        }
    }
}
