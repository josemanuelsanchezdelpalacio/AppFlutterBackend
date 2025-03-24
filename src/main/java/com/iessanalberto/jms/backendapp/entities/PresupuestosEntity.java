package com.iessanalberto.jms.backendapp.entities;

import com.iessanalberto.jms.backendapp.services.EncriptacionService;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "presupuestos", schema = "public", catalog = "presupuesto_db")
public class PresupuestosEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "id_usuario", insertable = false, updatable = false)
    private Long idUsuario;

    @Column(name = "nombre")
    @Convert(converter = EncriptacionService.StringEncryptionConverter.class)
    private String nombre;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "cantidad")
    @Convert(converter = EncriptacionService.BigDecimalEncryptionConverter.class)
    private BigDecimal cantidad;

    @Column(name = "cantidad_gastada")
    @Convert(converter = EncriptacionService.BigDecimalEncryptionConverter.class)
    private BigDecimal cantidadGastada;

    @Column(name = "cantidad_restante")
    @Convert(converter = EncriptacionService.BigDecimalEncryptionConverter.class)
    private BigDecimal cantidadRestante;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", nullable = false)
    private UsuariosEntity usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }
}

