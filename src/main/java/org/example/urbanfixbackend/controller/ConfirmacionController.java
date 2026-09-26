package org.example.urbanfixbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;
import org.example.urbanfixbackend.security.SecurityUtils;
import org.example.urbanfixbackend.service.ConfirmacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/{reporteId}/confirmaciones")
@RequiredArgsConstructor
public class ConfirmacionController {

    private final ConfirmacionService confirmacionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> confirmarReporte(@PathVariable Long reporteId) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        confirmacionService.confirmarReporte(reporteId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarConfirmacion(@PathVariable Long reporteId) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        confirmacionService.eliminarConfirmacion(reporteId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getUsuariosQueConfirmaron(@PathVariable Long reporteId) {
        List<UsuarioResponseDTO> response = confirmacionService.getUsuariosQueConfirmaron(reporteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conteo")
    public ResponseEntity<Long> getConteoConfirmaciones(@PathVariable Long reporteId) {
        long conteo = confirmacionService.getConteoConfirmaciones(reporteId);
        return ResponseEntity.ok(conteo);
    }

    @GetMapping("/verificar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> usuarioHaConfirmado(@PathVariable Long reporteId) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        boolean haConfirmado = confirmacionService.usuarioHaConfirmado(reporteId, usuarioId);
        return ResponseEntity.ok(haConfirmado);
    }
}
