package com.sergio.login;

import java.time.LocalDateTime;

public record AuditoriaDTO(
        Long id,
        String entidad,
        Long idRegistro,
        String accion,
        String valorAnterior,
        String valorNuevo,
        String username,
        LocalDateTime fechaHora,
        String ipOrigen,
        String detalle
) {
    public static AuditoriaDTO from(Auditoria a) {
        return new AuditoriaDTO(
                a.getId(),
                a.getEntidad() != null ? a.getEntidad().name() : null,
                a.getIdRegistro(),
                a.getAccion() != null ? a.getAccion().name() : null,
                a.getValorAnterior(),
                a.getValorNuevo(),
                a.getUsername(),
                a.getFechaHora(),
                a.getIpOrigen(),
                a.getDetalle()
        );
    }
}
