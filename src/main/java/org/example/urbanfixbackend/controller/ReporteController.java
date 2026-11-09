package org.example.urbanfixbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ReporteUpdateDTO;
import org.example.urbanfixbackend.dto.response.ReporteDetailDTO;
import org.example.urbanfixbackend.dto.response.ReportePublicDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.security.SecurityUtils;
import org.example.urbanfixbackend.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteResponseDTO> createReporte(@Valid @RequestBody ReporteCreateDTO dto) {
        log.debug("User authorities: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        log.debug("Auth: {}", SecurityContextHolder.getContext().getAuthentication());
        Long usuarioId = SecurityUtils.getCurrentUserId();
        ReporteResponseDTO response = reporteService.createReporte(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteResponseDTO> createReporteWithFile(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam("zonaId") Long zonaId) {
        log.debug("User authorities: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        log.debug("Auth: {}", SecurityContextHolder.getContext().getAuthentication());
        Long usuarioId = SecurityUtils.getCurrentUserId();
        ReporteResponseDTO response = reporteService.createReporteWithFile(
                titulo, descripcion, latitud, longitud, foto, categoriaId, zonaId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping("/public")
    public ResponseEntity<List<ReportePublicDTO>> getAllPublicReportes(
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) String search) {

        List<ReportePublicDTO> response;

        if (search != null && !search.isBlank()) {
            response = reporteService.searchPublicReportes(search);
        } else if (estado != null) {
            response = reporteService.getPublicReportesByEstado(estado);
        } else if (categoriaId != null) {
            response = reporteService.getPublicReportesByCategoria(categoriaId);
        } else if (zonaId != null) {
            response = reporteService.getPublicReportesByZona(zonaId);
        } else {
            response = reporteService.getAllPublicReportes();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteResponseDTO> getReporteById(@PathVariable Long id) {
        ReporteResponseDTO response = reporteService.getReporteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ReportePublicDTO> getPublicReporteById(@PathVariable Long id) {
        ReportePublicDTO response = reporteService.getPublicReporteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detalle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteDetailDTO> getReporteDetailById(@PathVariable Long id) {
        ReporteDetailDTO response = reporteService.getReporteDetailById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-reportes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReporteResponseDTO>> getMisReportes() {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        List<ReporteResponseDTO> response = reporteService.getReportesByUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteResponseDTO> updateReporte(
            @PathVariable Long id,
            @Valid @RequestBody ReporteUpdateDTO dto) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        ReporteResponseDTO response = reporteService.updateReporte(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReporte(@PathVariable Long id) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        reporteService.deleteReporte(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN_MUNICIPAL', 'TECNICO', 'SUPERVISOR')")
    public ResponseEntity<Void> cambiarEstadoReporte(
            @PathVariable Long id,
            @RequestBody EstadoReporte nuevoEstado) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        reporteService.cambiarEstado(id, nuevoEstado, usuarioId);
        return ResponseEntity.ok().build();
    }
}
