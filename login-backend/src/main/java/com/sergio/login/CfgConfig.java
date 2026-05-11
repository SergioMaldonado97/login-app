package com.sergio.login;

import jakarta.persistence.*;

@Entity
@Table(name = "CFG_CONFIG", schema = "BRM_CONFIG")
public class CfgConfig {

    @Id
    @Column(name = "CFG_CONFIG_VAR")
    private String var;

    @Column(name = "CFG_CONFIG_VALOR", nullable = false)
    private String valor;

    @Column(name = "CFG_CONFIG_GRUPO", nullable = false)
    private String grupo;

    public CfgConfig() {}

    public String getVar()   { return var; }
    public String getValor() { return valor; }
    public String getGrupo() { return grupo; }
}
