package com.iessanalberto.jms.backendapp.repository;

import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<UsuariosEntity, Long> {

    @Query("SELECT u FROM UsuariosEntity u WHERE u.email = :email")
    Optional<UsuariosEntity> buscarPorEmail(@Param("email") String email);
}

