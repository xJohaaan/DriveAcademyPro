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

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_matricula")
public class PagoMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    @Column(nullable = false, length = 150)
    private String concepto;

    @Column(nullable = false)
    private Double monto;

    @Column(name = "horas_incluidas", nullable = false)
    private Integer horasIncluidas;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    public PagoMatricula() {
    }

    public PagoMatricula(Usuario estudiante, Usuario registradoPor, String concepto,
                         Double monto, Integer horasIncluidas, LocalDateTime fechaPago) {
        this.estudiante = estudiante;
        this.registradoPor = registradoPor;
        this.concepto = concepto;
        this.monto = monto;
        this.horasIncluidas = horasIncluidas;
        this.fechaPago = fechaPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Usuario estudiante) {
        this.estudiante = estudiante;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Integer getHorasIncluidas() {
        return horasIncluidas;
    }

    public void setHorasIncluidas(Integer horasIncluidas) {
        this.horasIncluidas = horasIncluidas;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
