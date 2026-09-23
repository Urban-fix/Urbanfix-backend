package org.example.urbanfixbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zonas", uniqueConstraints = {
    @UniqueConstraint(columnNames = "distrito")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String distrito;
    
    @Column(length = 255)
    private String coordenadasReferencia;
}
