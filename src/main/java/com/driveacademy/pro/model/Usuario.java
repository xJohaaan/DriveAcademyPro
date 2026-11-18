package com.driveacademy.pro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "licencia_categoria", length = 5)
    private String licenciaCategoria;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    public Usuario() {
    }

    public Usuario(String nombreCompleto, String email, String password, String licenciaCategoria, Rol rol) {
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.password = password;
        this.licenciaCategoria = licenciaCategoria;
        this.rol = rol;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLicenciaCategoria() {
        return licenciaCategoria;
    }

    public void setLicenciaCategoria(String licenciaCategoria) {
        this.licenciaCategoria = licenciaCategoria;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /** Iniciales para el avatar: "Laura Gómez" -> "LG". */
    public String getIniciales() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "?";
        }
        String[] partes = nombreCompleto.trim().split("\\s+");
        String iniciales = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciales += partes[1].substring(0, 1);
        }
        return iniciales.toUpperCase();
    }
}
