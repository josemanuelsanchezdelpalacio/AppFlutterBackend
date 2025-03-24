package com.iessanalberto.jms.backendapp.entities;

import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TipoTransacciones;
import com.iessanalberto.jms.backendapp.services.EncriptacionService;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones", schema = "public", catalog = "presupuesto_db")
public class TransaccionesEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Convert(converter = EncriptacionService.StringEncryptionConverter.class)
    @Column(name = "nombre")
    private String nombre;

    @Basic
    @Convert(converter = EncriptacionService.BigDecimalEncryptionConverter.class)
    @Column(name = "cantidad")
    private BigDecimal cantidad;

    @Basic
    @Convert(converter = EncriptacionService.StringEncryptionConverter.class)
    @Column(name = "descripcion")
    private String descripcion;

    @Basic
    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private TipoTransacciones tipo;

    @Basic
    @Column(name = "categoria")
    private String categoria;

    @Basic
    @Column(name = "fecha_transaccion")
    private LocalDate fechaTransaccion;

    @Basic
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Basic
    @Column(name = "transaccion_recurrente")
    private Boolean transaccionRecurrente;

    @Basic
    @Column(name = "frecuencia_recurrencia")
    private String frecuenciaRecurrencia;

    @Basic
    @Column(name = "fecha_finalizacion_recurrencia")
    private LocalDateTime fechaFinalizacionRecurrencia;

    @Basic
    @Column(name = "imagen_url")
    private String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", nullable = false)
    private UsuariosEntity usuario;

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

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoTransacciones getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacciones tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDate getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(LocalDate fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getTransaccionRecurrente() {
        return transaccionRecurrente;
    }

    public void setTransaccionRecurrente(Boolean transaccionRecurrente) {
        this.transaccionRecurrente = transaccionRecurrente;
    }

    public String getFrecuenciaRecurrencia() {
        return frecuenciaRecurrencia;
    }

    public void setFrecuenciaRecurrencia(String frecuenciaRecurrencia) {
        this.frecuenciaRecurrencia = frecuenciaRecurrencia;
    }

    public LocalDateTime getFechaFinalizacionRecurrencia() {
        return fechaFinalizacionRecurrencia;
    }

    public void setFechaFinalizacionRecurrencia(LocalDateTime fechaFinalizacionRecurrencia) {
        this.fechaFinalizacionRecurrencia = fechaFinalizacionRecurrencia;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }
}

