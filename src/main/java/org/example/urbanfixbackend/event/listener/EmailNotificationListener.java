package org.example.urbanfixbackend.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void handleUsuarioRegistradoEvent(UsuarioRegistradoEvent event) {
        log.info("Procesando evento UsuarioRegistradoEvent para usuario: {}", 
                event.getUsuario().getEmail());
        
        emailService.sendWelcomeEmail(
                event.getUsuario().getEmail(),
                event.getUsuario().getNombre()
        );
    }
}
