package org.example.urbanfixbackend.entity;

import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "estado_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoHistorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private EstadoReporte estadoAnterior;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private EstadoReporte estadoNuevo;
    
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @PrePersist
    protected void onCreate() {
        fechaCambio = LocalDateTime.now();
    }
}
