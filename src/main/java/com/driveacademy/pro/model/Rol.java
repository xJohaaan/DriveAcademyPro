package com.driveacademy.pro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Rol {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String INSTRUCTOR = "ROLE_INSTRUCTOR";
    public static final String RECEPCIONISTA = "ROLE_RECEPCIONISTA";
    public static final String ESTUDIANTE = "ROLE_ESTUDIANTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public Rol() {
    }

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Nombre legible para la interfaz: "ROLE_RECEPCIONISTA" -> "Recepcionista". */
    public String getEtiqueta() {
        if (nombre == null) {
            return "";
        }
        String base = nombre.replace("ROLE_", "").toLowerCase();
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
