package org.example.urbanfixbackend.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.event.EstadoCambiadoEvent;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EstadoNotificationListener {

    private final EmailService emailService;
    private final ReporteRepository reporteRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onEstadoCambiado(EstadoCambiadoEvent event) {
        Long reporteId = event.getReporte().getId();
        reporteRepository.findById(reporteId).ifPresent(reporte -> {
            log.info("[{}] Notificando cambio de estado del reporte {}: {} -> {}",
                    Thread.currentThread().getName(), reporteId,
                    event.getEstadoAnterior(), event.getEstadoNuevo());
            emailService.sendEstadoCambiadoEmail(
                    reporte.getUsuario().getEmail(),
                    reporte.getTitulo(),
                    event.getEstadoAnterior().name(),
                    event.getEstadoNuevo().name()
            );
        });
    }
}