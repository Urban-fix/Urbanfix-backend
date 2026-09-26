package org.example.urbanfixbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirmaciones", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reporte_id", "usuario_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Confirmacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha_confirmacion", nullable = false, updatable = false)
    private LocalDateTime fechaConfirmacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @PrePersist
    protected void onCreate() {
        fechaConfirmacion = LocalDateTime.now();
    }
}
