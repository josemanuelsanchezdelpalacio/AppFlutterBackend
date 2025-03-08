package com.iessanalberto.jms.backendapp.entities;

import com.iessanalberto.jms.backendapp.DTO.AuthDTO.AuthProvider;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "usuarios", schema = "public", catalog = "presupuesto_db")
public class UsuariosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Basic
    @Column(name = "password", nullable = true)
    private String password;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Basic
    @Column(name = "id_usuario_firebase", unique = true)
    private String idUsuarioFirebase;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

}

