package com.iessanalberto.jms.backendapp.entities;

import com.iessanalberto.jms.backendapp.services.EncriptacionService;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metas_ahorro", schema = "public", catalog = "presupuesto_db")
public class MetasAhorroEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "id_usuario", insertable = false, updatable = false)
    private Long usuarioId;

    @Basic
    @Column(name = "nombre")
    @Convert(converter = EncriptacionService.StringEncryptionConverter.class)
    private String nombre;

    @Basic
    @Column(name = "categoria")
    private String categoria;

    @Basic
    @Column(name = "cantidad_objetivo")
    @Convert(converter = EncriptacionService.BigDecimalEncryptionConverter.class)
    private BigDecimal cantidadObjetivo;

    @Basic
    @Column(name = "cantidad_actual")
    @Convert(converter = EncriptacionService.BigDecimalEncryptionConverter.class)
    private BigDecimal cantidadActual;

    @Basic
    @Column(name = "fecha_objetivo")
    private LocalDate fechaObjetivo;

    @Basic
    @Column(name = "completada")
    private boolean completada;

    @Basic
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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

    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }
}


