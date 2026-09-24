package org.example.urbanfixbackend.event;

import org.example.urbanfixbackend.entity.Reporte;
import org.springframework.context.ApplicationEvent;

public class ReporteCreadoEvent extends ApplicationEvent {

    private final Reporte reporte;

    public ReporteCreadoEvent(Reporte reporte) {
        super(reporte);
        this.reporte = reporte;
    }

    public Reporte getReporte() {
        return reporte;
    }
}
