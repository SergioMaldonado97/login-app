package com.sergio.login;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUD_AUDITORIA", schema = "BRM_CONFIG")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EntidadAuditable entidad;

    @Column(name = "id_registro")
    private Long idRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccionAuditoria accion;

    @Column(name = "valor_anterior", columnDefinition = "NVARCHAR(MAX)")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "NVARCHAR(MAX)")
    private String valorNuevo;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "ip_origen", nullable = false, length = 45)
    private String ipOrigen;

    @Column(length = 500)
    private String detalle;

    protected Auditoria() {}

    public Auditoria(EntidadAuditable entidad, Long idRegistro, AccionAuditoria accion,
                     String valorAnterior, String valorNuevo,
                     String username, LocalDateTime fechaHora,
                     String ipOrigen, String detalle) {
        this.entidad = entidad;
        this.idRegistro = idRegistro;
        this.accion = accion;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.username = username;
        this.fechaHora = fechaHora;
        this.ipOrigen = ipOrigen;
        this.detalle = detalle;
    }

    public Long getId() { return id; }
    public EntidadAuditable getEntidad() { return entidad; }
    public Long getIdRegistro() { return idRegistro; }
    public AccionAuditoria getAccion() { return accion; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNuevo() { return valorNuevo; }
    public String getUsername() { return username; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getIpOrigen() { return ipOrigen; }
    public String getDetalle() { return detalle; }
}
