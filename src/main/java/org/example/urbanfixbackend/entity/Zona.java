package org.example.urbanfixbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zonas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    private String nombre;
    
    @Size(max = 255)
    private String coordenadasReferencia;
}
