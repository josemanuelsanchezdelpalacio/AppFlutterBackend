package com.iessanalberto.jms.backendapp.repository;

import com.iessanalberto.jms.backendapp.entities.PresupuestosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestosRepository extends JpaRepository<PresupuestosEntity, Long> {

    @Query("SELECT p FROM PresupuestosEntity p WHERE p.usuario.id = :idUsuario")
    List<PresupuestosEntity> findByUsuarioId(@Param("idUsuario") Long idUsuario);

}





