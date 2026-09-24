package org.example.urbanfixbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ReporteUpdateDTO;
import org.example.urbanfixbackend.dto.response.ReporteDetailDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.security.SecurityUtils;
import org.example.urbanfixbackend.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping
    public ResponseEntity<ReporteResponseDTO> createReporte(@Valid @RequestBody ReporteCreateDTO dto) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        ReporteResponseDTO response = reporteService.createReporte(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReporteResponseDTO>> getAllReportes(
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String search) {
        
        List<ReporteResponseDTO> response;
        
        if (search != null && !search.isBlank()) {
            response = reporteService.searchReportes(search);
        } else if (estado != null) {
            response = reporteService.getReportesByEstado(estado);
        } else if (categoriaId != null) {
            response = reporteService.getReportesByCategoria(categoriaId);
        } else if (zonaId != null) {
            response = reporteService.getReportesByZona(zonaId);
        } else if (usuarioId != null) {
            response = reporteService.getReportesByUsuario(usuarioId);
        } else {
            response = reporteService.getAllReportes();
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponseDTO> getReporteById(@PathVariable Long id) {
        ReporteResponseDTO response = reporteService.getReporteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ReporteDetailDTO> getReporteDetailById(@PathVariable Long id) {
        ReporteDetailDTO response = reporteService.getReporteDetailById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-reportes")
    public ResponseEntity<List<ReporteResponseDTO>> getMisReportes() {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        List<ReporteResponseDTO> response = reporteService.getReportesByUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReporteResponseDTO> updateReporte(
            @PathVariable Long id,
            @Valid @RequestBody ReporteUpdateDTO dto) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        ReporteResponseDTO response = reporteService.updateReporte(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReporte(@PathVariable Long id) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        reporteService.deleteReporte(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN_MUNICIPAL')")
    public ResponseEntity<Void> cambiarEstadoReporte(
            @PathVariable Long id,
            @RequestBody EstadoReporte nuevoEstado) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        reporteService.cambiarEstado(id, nuevoEstado, usuarioId);
        return ResponseEntity.ok().build();
    }
}
