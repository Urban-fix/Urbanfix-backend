package org.example.urbanfixbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.ComentarioCreateDTO;
import org.example.urbanfixbackend.dto.response.ComentarioResponseDTO;
import org.example.urbanfixbackend.security.SecurityUtils;
import org.example.urbanfixbackend.service.ComentarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes/{reporteId}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> crearComentario(
            @PathVariable Long reporteId,
            @Valid @RequestBody ComentarioCreateDTO dto) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        ComentarioResponseDTO response = comentarioService.crearComentario(reporteId, dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> getComentariosByReporteId(@PathVariable Long reporteId) {
        List<ComentarioResponseDTO> response = comentarioService.getComentariosByReporteId(reporteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> eliminarComentario(
            @PathVariable Long reporteId,
            @PathVariable Long comentarioId) {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        comentarioService.eliminarComentario(comentarioId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
