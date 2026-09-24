package org.example.urbanfixbackend.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.event.EstadoCambiadoEvent;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EstadoNotificationListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEstadoCambiadoEvent(EstadoCambiadoEvent event) {
        log.info("Procesando evento EstadoCambiadoEvent para reporte: {}", 
                event.getReporte().getId());
        
        emailService.sendEstadoCambiadoEmail(
                event.getReporte().getUsuario().getEmail(),
                event.getReporte().getTitulo(),
                event.getEstadoNuevo().name()
        );
    }
}
