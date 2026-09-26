package org.example.urbanfixbackend.entity;

import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(nullable = false, length = 2000)
    private String descripcion;
    
    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;
    
    @Column(length = 500)
    private String fotoUrl;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estadoActual;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;
    
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadoHistorial> estadoHistorialList = new ArrayList<>();
    
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Confirmacion> confirmacionList = new ArrayList<>();
    
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarioList = new ArrayList<>();
    
    @PrePersist //ejecuta este método justo antes de insertar el objeto en la base de datos
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estadoActual == null) {
            estadoActual = EstadoReporte.REPORTADO;
        }
    }
}
