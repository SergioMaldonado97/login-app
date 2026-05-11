package com.sergio.login;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaSpec {

    private AuditoriaSpec() {}

    public static Specification<Auditoria> build(AuditoriaFiltrosDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.entidad() != null && !filtros.entidad().isBlank()) {
                try {
                    EntidadAuditable entidad = EntidadAuditable.valueOf(filtros.entidad().toUpperCase());
                    predicates.add(cb.equal(root.get("entidad"), entidad));
                } catch (IllegalArgumentException ignored) {
                    // Valor de entidad no reconocido: filtro ignorado
                }
            }

            if (filtros.accion() != null && !filtros.accion().isBlank()) {
                try {
                    AccionAuditoria accion = AccionAuditoria.valueOf(filtros.accion().toUpperCase());
                    predicates.add(cb.equal(root.get("accion"), accion));
                } catch (IllegalArgumentException ignored) {
                    // Valor de accion no reconocido: filtro ignorado
                }
            }

            if (filtros.username() != null && !filtros.username().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("username")),
                        "%" + filtros.username().toLowerCase() + "%"
                ));
            }

            if (filtros.idRegistro() != null) {
                predicates.add(cb.equal(root.get("idRegistro"), filtros.idRegistro()));
            }

            if (filtros.fechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), filtros.fechaDesde()));
            }

            if (filtros.fechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), filtros.fechaHasta()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
