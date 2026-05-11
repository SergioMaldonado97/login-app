package com.sergio.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository repo;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    public AuditoriaService(AuditoriaRepository repo,
                            ConfigService configService,
                            ObjectMapper objectMapper) {
        this.repo = repo;
        this.configService = configService;
        this.objectMapper = objectMapper;
    }

    public void registrar(EntidadAuditable entidad, Long idRegistro,
                          AccionAuditoria accion,
                          Object estadoAnterior, Object estadoNuevo,
                          String username, HttpServletRequest request,
                          String detalle) {
        try {
            String auditEnabled = configService.getValor("AUDIT_ENABLED", "true");
            if (!"true".equalsIgnoreCase(auditEnabled)) {
                return;
            }

            Auditoria reg = new Auditoria(
                    entidad,
                    idRegistro,
                    accion,
                    toJson(estadoAnterior),
                    toJson(estadoNuevo),
                    username != null ? username : "DESCONOCIDO",
                    LocalDateTime.now(ZoneOffset.UTC),
                    extractIp(request),
                    detalle
            );
            repo.save(reg);
        } catch (Exception e) {
            log.error("[AUDITORIA] Error al registrar evento {}/{}: {}", entidad, accion, e.getMessage());
        }
    }

    public Page<Auditoria> buscar(AuditoriaFiltrosDTO filtros, Pageable pageable) {
        return repo.findAll(AuditoriaSpec.build(filtros), pageable);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("[AUDITORIA] No se pudo serializar el objeto a JSON: {}", e.getMessage());
            return null;
        }
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) return "0.0.0.0";
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
