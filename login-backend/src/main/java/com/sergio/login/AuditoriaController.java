package com.sergio.login;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<AuditoriaDTO>> buscar(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long idRegistro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaHora").descending());

        AuditoriaFiltrosDTO filtros = new AuditoriaFiltrosDTO(
                entidad,
                accion,
                username,
                idRegistro,
                fechaDesde != null ? fechaDesde.atStartOfDay() : null,
                fechaHasta != null ? fechaHasta.atTime(23, 59, 59) : null
        );

        Page<AuditoriaDTO> resultado = service.buscar(filtros, pageable)
                .map(AuditoriaDTO::from);

        return ResponseEntity.ok(resultado);
    }
}
