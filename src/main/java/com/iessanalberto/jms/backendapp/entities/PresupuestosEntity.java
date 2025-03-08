package com.iessanalberto.jms.backendapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "presupuestos", schema = "public", catalog = "presupuesto_db")
public class PresupuestosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuariosEntity usuario;

    @Basic
    @Column(name = "nombre", nullable = false) // Added nombre column
    private String nombre;

    @Basic
    @Column(name = "categoria", nullable = false)
    private String categoria;

    @Basic
    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad;

    @Basic
    @Column(name = "cantidad_gastada")
    private BigDecimal cantidadGastada;

    @Basic
    @Column(name = "cantidad_restante")
    private BigDecimal cantidadRestante;

    @Basic
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Basic
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Basic
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCantidadGastada() {
        return cantidadGastada;
    }

    public void setCantidadGastada(BigDecimal cantidadGastada) {
        this.cantidadGastada = cantidadGastada;
    }

    public BigDecimal getCantidadRestante() {
        return cantidadRestante;
    }

    public void setCantidadRestante(BigDecimal cantidadRestante) {
        this.cantidadRestante = cantidadRestante;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

