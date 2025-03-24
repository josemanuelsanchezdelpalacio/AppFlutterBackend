package com.iessanalberto.jms.backendapp.repository;

import com.iessanalberto.jms.backendapp.entities.TransaccionesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransaccionesRepository extends JpaRepository<TransaccionesEntity, Long> {

    @Query("SELECT t FROM TransaccionesEntity t WHERE t.usuario.id = :idUsuario")
    List<TransaccionesEntity> buscarPorUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query("SELECT t FROM TransaccionesEntity t WHERE t.usuario.id = :idUsuario AND t.fechaTransaccion BETWEEN :inicioFecha AND :finFecha")
    List<TransaccionesEntity> buscarTransaccionPorFecha(
            @Param("idUsuario") Long idUsuario,
            @Param("inicioFecha") LocalDate inicioFecha,
            @Param("finFecha") LocalDate finFecha
    );
}

