package org.example.urbanfixbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categorias", uniqueConstraints = {
    @UniqueConstraint(columnNames = "nombre")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
    
    @Column(nullable = false, length = 500)
    private String descripcion;
}
