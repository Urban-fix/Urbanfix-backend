package org.example.urbanfixbackend.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.event.UsuarioRegistradoEvent;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        Usuario usuario = event.getUsuario();
        log.info("[{}] Enviando email de bienvenida a {}",
                Thread.currentThread().getName(), usuario.getEmail());
        emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getNombre());
    }
}