package com.iessanalberto.jms.backendapp.entities;

import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TipoTransacciones;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Table(name = "transacciones", schema = "public", catalog = "presupuesto_db")
public class TransaccionesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuariosEntity usuario;

    @Basic
    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad;

    @Basic
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoTransacciones tipo;

    @Basic
    @Column(name = "categoria", nullable = false)
    private String categoria;

    @Basic
    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDate fechaTransaccion;

    @Basic
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
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

    @PrePersist
    protected void onCreate() {
        if (fechaTransaccion == null) {
            fechaTransaccion = LocalDate.now();
        }
        fechaCreacion = LocalDateTime.now();
    }

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
}

