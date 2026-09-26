package org.example.urbanfixbackend.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendWelcomeEmail(String to, String nombre) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Bienvenido a UrbanFix 🏙️");
            
            Context context = new Context();
            context.setVariable("nombre", nombre);
            
            String htmlContent = templateEngine.process("email/welcome", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a: {}", to, e);
        }
    }

    @Override
    public void sendEstadoCambiadoEmail(String to, String reporteTitulo, String estadoAnterior, String estadoNuevo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Actualización de reporte: " + reporteTitulo);
            
            Context context = new Context();
            context.setVariable("reporteTitulo", reporteTitulo);
            context.setVariable("estadoAnterior", estadoAnterior);
            context.setVariable("estadoNuevo", estadoNuevo);
            
            String htmlContent = templateEngine.process("email/estado-cambiado", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de cambio de estado enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de cambio de estado a: {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String nombre, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Recuperación de Contraseña - UrbanFix 🔐");
            
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("resetLink", resetLink);
            
            String htmlContent = templateEngine.process("email/recuperar-password", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de recuperación de contraseña enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación de contraseña a: {}", to, e);
        }
    }
}
