package com.sergio.login;

import java.time.LocalDateTime;

public record AuditoriaFiltrosDTO(
        String entidad,
        String accion,
        String username,
        Long idRegistro,
        LocalDateTime fechaDesde,
        LocalDateTime fechaHasta
) {}
