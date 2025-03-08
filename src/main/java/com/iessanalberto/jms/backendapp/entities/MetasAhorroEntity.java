package com.iessanalberto.jms.backendapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "metas_ahorro", schema = "public", catalog = "presupuesto_db")
public class MetasAhorroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuariosEntity usuario;

    @Basic
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Basic
    @Column(name = "categoria", nullable = false) // Added categoria column
    private String categoria;

    @Basic
    @Column(name = "cantidad_objetivo", nullable = false)
    private BigDecimal cantidadObjetivo;

    @Basic
    @Column(name = "cantidad_actual", nullable = false)
    private BigDecimal cantidadActual;

    @Basic
    @Column(name = "fecha_objetivo", nullable = false)
    private LocalDate fechaObjetivo;

    @Basic
    @Column(name = "completada", nullable = false)
    private boolean completada;

    @Basic
    @Column(name = "fecha_creacion", nullable = false)
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

    public BigDecimal getCantidadObjetivo() {
        return cantidadObjetivo;
    }

    public void setCantidadObjetivo(BigDecimal cantidadObjetivo) {
        this.cantidadObjetivo = cantidadObjetivo;
    }

    public BigDecimal getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(BigDecimal cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public LocalDate getFechaObjetivo() {
        return fechaObjetivo;
    }

    public void setFechaObjetivo(LocalDate fechaObjetivo) {
        this.fechaObjetivo = fechaObjetivo;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

