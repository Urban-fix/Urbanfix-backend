package org.example.urbanfixbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.ZonaCreateDTO;
import org.example.urbanfixbackend.dto.response.ZonaResponseDTO;
import org.example.urbanfixbackend.service.ZonaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaService zonaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_MUNICIPAL')")
    public ResponseEntity<ZonaResponseDTO> createZona(@Valid @RequestBody ZonaCreateDTO dto) {
        ZonaResponseDTO response = zonaService.createZona(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ZonaResponseDTO>> getAllZonas() {
        List<ZonaResponseDTO> response = zonaService.getAllZonas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZonaResponseDTO> getZonaById(@PathVariable Long id) {
        ZonaResponseDTO response = zonaService.getZonaById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_MUNICIPAL')")
    public ResponseEntity<ZonaResponseDTO> updateZona(
            @PathVariable Long id,
            @Valid @RequestBody ZonaCreateDTO dto) {
        ZonaResponseDTO response = zonaService.updateZona(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_MUNICIPAL')")
    public ResponseEntity<Void> deleteZona(@PathVariable Long id) {
        zonaService.deleteZona(id);
        return ResponseEntity.noContent().build();
    }
}
