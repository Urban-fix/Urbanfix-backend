package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class EmailServiceFileImpl implements EmailService {

    private final TemplateEngine templateEngine;

    @Value("${email.output.directory:email-output}")
    private String emailOutputDirectory;

    public void setEmailOutputDirectory(String emailOutputDirectory) {
        this.emailOutputDirectory = emailOutputDirectory;
    }

    @Override
    public void sendWelcomeEmail(String to, String nombre) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            String htmlContent = templateEngine.process("email/welcome", context);
            
            saveEmailToFile(to, "Bienvenido a UrbanFix 🏙️", htmlContent);
            log.info("Email de bienvenida guardado en archivo para: {}", to);
        } catch (Exception e) {
            log.error("Error al guardar email de bienvenida para: {}", to, e);
        }
    }

    @Override
    public void sendEstadoCambiadoEmail(String to, String reporteTitulo, String estadoAnterior, String estadoNuevo) {
        try {
            Context context = new Context();
            context.setVariable("reporteTitulo", reporteTitulo);
            context.setVariable("estadoAnterior", estadoAnterior);
            context.setVariable("estadoNuevo", estadoNuevo);
            String htmlContent = templateEngine.process("email/estado-cambiado", context);
            
            saveEmailToFile(to, "Actualización de reporte: " + reporteTitulo, htmlContent);
            log.info("Email de cambio de estado guardado en archivo para: {}", to);
        } catch (Exception e) {
            log.error("Error al guardar email de cambio de estado para: {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String nombre, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("resetLink", resetLink);
            String htmlContent = templateEngine.process("email/recuperar-password", context);
            
            saveEmailToFile(to, "Recuperación de Contraseña - UrbanFix 🔐", htmlContent);
            log.info("Email de recuperación de contraseña guardado en archivo para: {}", to);
        } catch (Exception e) {
            log.error("Error al guardar email de recuperación de contraseña para: {}", to, e);
        }
    }

    private void saveEmailToFile(String to, String subject, String htmlContent) throws IOException {
        Path outputDir = Paths.get(emailOutputDirectory);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = String.format("%s-%s.txt", timestamp, to.replace("@", "_").replace(".", "_"));
        Path filePath = outputDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write("TO: " + to + "\n");
            writer.write("SUBJECT: " + subject + "\n");
            writer.write("TIMESTAMP: " + LocalDateTime.now() + "\n");
            writer.write("========================================\n\n");
            writer.write(htmlContent);
        }

        log.info("Email guardado en: {}", filePath);
    }
}
