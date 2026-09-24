package org.example.urbanfixbackend.event;

import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.springframework.context.ApplicationEvent;

public class EstadoCambiadoEvent extends ApplicationEvent {

    private final Reporte reporte;
    private final EstadoReporte estadoAnterior;
    private final EstadoReporte estadoNuevo;

    public EstadoCambiadoEvent(Reporte reporte, EstadoReporte estadoAnterior, EstadoReporte estadoNuevo) {
        super(reporte);
        this.reporte = reporte;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public EstadoReporte getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoReporte getEstadoNuevo() {
        return estadoNuevo;
    }
}
