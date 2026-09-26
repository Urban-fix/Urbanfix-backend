package org.example.urbanfixbackend.service;

import jakarta.mail.internet.MimeMessage;
import org.example.urbanfixbackend.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_Success() throws Exception {
        when(templateEngine.process(eq("email/welcome"), any(Context.class))).thenReturn("<html>Welcome</html>");

        emailService.sendWelcomeEmail("test@example.com", "Juan");

        verify(templateEngine, times(1)).process(eq("email/welcome"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_Exception() throws Exception {
        when(templateEngine.process(eq("email/welcome"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        emailService.sendWelcomeEmail("test@example.com", "Juan");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEstadoCambiadoEmail_Success() throws Exception {
        when(templateEngine.process(eq("email/estado-cambiado"), any(Context.class))).thenReturn("<html>Estado cambiado</html>");

        emailService.sendEstadoCambiadoEmail("test@example.com", "Bache en calle", "REPORTADO", "EN_PROGRESO");

        verify(templateEngine, times(1)).process(eq("email/estado-cambiado"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendEstadoCambiadoEmail_Exception() throws Exception {
        when(templateEngine.process(eq("email/estado-cambiado"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        emailService.sendEstadoCambiadoEmail("test@example.com", "Bache en calle", "REPORTADO", "EN_PROGRESO");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Success() throws Exception {
        when(templateEngine.process(eq("email/recuperar-password"), any(Context.class))).thenReturn("<html>Reset password</html>");

        emailService.sendPasswordResetEmail("test@example.com", "Juan", "https://urbanfix.com/reset?token=abc123");

        verify(templateEngine, times(1)).process(eq("email/recuperar-password"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_Exception() throws Exception {
        when(templateEngine.process(eq("email/recuperar-password"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        emailService.sendPasswordResetEmail("test@example.com", "Juan", "https://urbanfix.com/reset?token=abc123");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
