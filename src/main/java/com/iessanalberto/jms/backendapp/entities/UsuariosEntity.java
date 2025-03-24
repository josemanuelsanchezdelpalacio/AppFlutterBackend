package com.iessanalberto.jms.backendapp.entities;

import com.iessanalberto.jms.backendapp.DTO.AuthDTO.AuthProvider;
import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(name = "usuarios", schema = "public", catalog = "presupuesto_db")
public class UsuariosEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "password")
    private String password;

    @Basic
    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Basic
    @Column(name = "id_usuario_firebase")
    private String idUsuarioFirebase;

    @OneToMany(mappedBy = "usuario")
    private Collection<MetasAhorroEntity> metasAhorrosById;

    @OneToMany(mappedBy = "usuario")
    private Collection<PresupuestosEntity> presupuestosById;

    @OneToMany(mappedBy = "usuario")
    private Collection<TransaccionesEntity> transaccionesById;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getIdUsuarioFirebase() {
        return idUsuarioFirebase;
    }

    public void setIdUsuarioFirebase(String idUsuarioFirebase) {
        this.idUsuarioFirebase = idUsuarioFirebase;
    }

    public Collection<MetasAhorroEntity> getMetasAhorrosById() {
        return metasAhorrosById;
    }

    public void setMetasAhorrosById(Collection<MetasAhorroEntity> metasAhorrosById) {
        this.metasAhorrosById = metasAhorrosById;
    }

    public Collection<PresupuestosEntity> getPresupuestosById() {
        return presupuestosById;
    }

    public void setPresupuestosById(Collection<PresupuestosEntity> presupuestosById) {
        this.presupuestosById = presupuestosById;
    }

    public Collection<TransaccionesEntity> getTransaccionesById() {
        return transaccionesById;
    }

    public void setTransaccionesById(Collection<TransaccionesEntity> transaccionesById) {
        this.transaccionesById = transaccionesById;
    }
}

