package com.iessanalberto.jms.backendapp.repository;

import com.iessanalberto.jms.backendapp.entities.MetasAhorroEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetasAhorroRepository extends JpaRepository<MetasAhorroEntity, Long> {

    // Método correcto que Spring Data JPA reconoce automáticamente
    List<MetasAhorroEntity> findByUsuarioId(Long idUsuario);
    List<MetasAhorroEntity> findByUsuarioIdAndCompletadaFalse(Long idUsuario);

}


