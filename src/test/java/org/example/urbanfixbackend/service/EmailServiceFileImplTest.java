package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.service.impl.EmailServiceFileImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceFileImplTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceFileImpl emailServiceFileImpl;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("email-test");
        emailServiceFileImpl = new EmailServiceFileImpl(templateEngine);
        emailServiceFileImpl.setEmailOutputDirectory(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Test
    void sendWelcomeEmail_Success() {
        when(templateEngine.process(eq("email/welcome"), any(Context.class))).thenReturn("<html>Welcome</html>");

        emailServiceFileImpl.sendWelcomeEmail("test@example.com", "Juan");

        verify(templateEngine, times(1)).process(eq("email/welcome"), any(Context.class));
    }

    @Test
    void sendWelcomeEmail_Exception() {
        when(templateEngine.process(eq("email/welcome"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        emailServiceFileImpl.sendWelcomeEmail("test@example.com", "Juan");

        verify(templateEngine, times(1)).process(eq("email/welcome"), any(Context.class));
    }

    @Test
    void sendEstadoCambiadoEmail_Success() {
        when(templateEngine.process(eq("email/estado-cambiado"), any(Context.class))).thenReturn("<html>Estado cambiado</html>");

        emailServiceFileImpl.sendEstadoCambiadoEmail("test@example.com", "Bache en calle", "REPORTADO", "EN_PROGRESO");

        verify(templateEngine, times(1)).process(eq("email/estado-cambiado"), any(Context.class));
    }

    @Test
    void sendEstadoCambiadoEmail_Exception() {
        when(templateEngine.process(eq("email/estado-cambiado"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        emailServiceFileImpl.sendEstadoCambiadoEmail("test@example.com", "Bache en calle", "REPORTADO", "EN_PROGRESO");

        verify(templateEngine, times(1)).process(eq("email/estado-cambiado"), any(Context.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        when(templateEngine.process(eq("email/recuperar-password"), any(Context.class))).thenReturn("<html>Reset password</html>");

        emailServiceFileImpl.sendPasswordResetEmail("test@example.com", "Juan", "https://urbanfix.com/reset?token=abc123");

        verify(templateEngine, times(1)).process(eq("email/recuperar-password"), any(Context.class));
    }

    @Test
    void sendPasswordResetEmail_Exception() {
        when(templateEngine.process(eq("email/recuperar-password"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        emailServiceFileImpl.sendPasswordResetEmail("test@example.com", "Juan", "https://urbanfix.com/reset?token=abc123");

        verify(templateEngine, times(1)).process(eq("email/recuperar-password"), any(Context.class));
    }
}
