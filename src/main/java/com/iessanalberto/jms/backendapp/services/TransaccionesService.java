package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.entities.TransaccionesEntity;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import com.iessanalberto.jms.backendapp.repository.TransaccionesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionesService {

    private final TransaccionesRepository transaccionesRepository;
    private final AuthRepository authRepository;
    private final MetasAhorroService metasAhorroService;
    private final PresupuestosService presupuestosService;

    public TransaccionesService(TransaccionesRepository transaccionesRepository,
                                AuthRepository authRepository,
                                MetasAhorroService metasAhorroService,
                                PresupuestosService presupuestosService) {
        this.transaccionesRepository = transaccionesRepository;
        this.authRepository = authRepository;
        this.metasAhorroService = metasAhorroService;
        this.presupuestosService = presupuestosService;
    }

    public List<TransaccionesDTO> obtenerTransacciones(Long idUsuario) {
        return transaccionesRepository.buscarPorUsuarioId(idUsuario)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    public List<TransaccionesDTO> obtenerTransaccionesFecha(Long idUsuario, LocalDate inicioFecha, LocalDate finFecha) {
        return transaccionesRepository.buscarTransaccionPorFecha(idUsuario, inicioFecha, finFecha)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransaccionesDTO crearTransacciones(Long idUsuario, TransaccionesDTO dto) {
        UsuariosEntity usuario = obtenerUsuarioPorId(idUsuario);

        TransaccionesEntity transaction = new TransaccionesEntity();
        transaction.setUsuario(usuario);
        transaction.setCantidad(dto.getCantidad());
        transaction.setDescripcion(dto.getDescripcion());
        transaction.setTipo(dto.getTipoTransaccion());
        transaction.setCategoria(dto.getCategoria());
        transaction.setFechaTransaccion(dto.getFechaTransaccion());
        transaction.setFechaCreacion(LocalDateTime.now());
        transaction.setTransaccionRecurrente(dto.getTransaccionRecurrente());
        transaction.setFrecuenciaRecurrencia(dto.getFrecuenciaRecurrencia());
        transaction.setFechaFinalizacionRecurrencia(dto.getFechaFinalizacionRecurrencia());

        TransaccionesEntity savedTransaction = transaccionesRepository.save(transaction);

        // Actualizar metas y presupuestos
        actualizarMetasYPresupuestos(idUsuario, dto);

        return convertirDTO(savedTransaction);
    }

    @Transactional
    public TransaccionesDTO actualizarTransacciones(Long idUsuario, Long idTransaccion, TransaccionesDTO dto) {
        TransaccionesEntity transaction = transaccionesRepository.findById(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!transaction.getUsuario().getId().equals(idUsuario)) {
            throw new SecurityException("No autorizado para actualizar esta transacción");
        }

        // Obtener la transacción original para revertir sus efectos
        TransaccionesDTO transaccionOriginal = convertirDTO(transaction);

        // Revertir efectos de la transacción original
        revertirEfectosTransaccion(idUsuario, transaccionOriginal);

        // Actualizar la entidad con los nuevos valores
        transaction.setCantidad(dto.getCantidad());
        transaction.setDescripcion(dto.getDescripcion());
        transaction.setTipo(dto.getTipoTransaccion());
        transaction.setCategoria(dto.getCategoria());
        transaction.setFechaTransaccion(dto.getFechaTransaccion());
        transaction.setTransaccionRecurrente(dto.getTransaccionRecurrente());
        transaction.setFrecuenciaRecurrencia(dto.getFrecuenciaRecurrencia());
        transaction.setFechaFinalizacionRecurrencia(dto.getFechaFinalizacionRecurrencia());

        // Actualizar la transacción
        TransaccionesEntity updatedTransaction = transaccionesRepository.save(transaction);

        // Aplicar efectos de la transacción actualizada
        actualizarMetasYPresupuestos(idUsuario, dto);

        return convertirDTO(updatedTransaction);
    }

    @Transactional
    public void borrarTransacciones(Long idUsuario, Long idTransaccion) {
        TransaccionesEntity transaction = transaccionesRepository.findById(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!transaction.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para eliminar esta transacción");
        }

        // Obtener la transacción antes de eliminarla
        TransaccionesDTO transaccionAEliminar = convertirDTO(transaction);

        // Revertir efectos de la transacción
        revertirEfectosTransaccion(idUsuario, transaccionAEliminar);

        // Eliminar la transacción
        transaccionesRepository.delete(transaction);

    }

    private void actualizarMetasYPresupuestos(Long idUsuario, TransaccionesDTO dto) {
        // Actualizar metas de ahorro
        metasAhorroService.actualizarMetasPorTransaccion(idUsuario, dto);

        // Actualizar presupuestos
        presupuestosService.actualizarPresupuestosPorTransaccion(idUsuario, dto);
    }

    private void revertirEfectosTransaccion(Long idUsuario, TransaccionesDTO dto) {
        // Revertir efectos en metas de ahorro
        metasAhorroService.revertirEfectoTransaccion(idUsuario, dto);

        // Revertir efectos en presupuestos (asumiendo que tienes un método similar)
        presupuestosService.revertirEfectoTransaccion(idUsuario, dto);
    }

    private TransaccionesDTO convertirDTO(TransaccionesEntity transaccion) {
        return new TransaccionesDTO(
                transaccion.getId(),
                transaccion.getCantidad(),
                transaccion.getDescripcion(),
                transaccion.getTipo(),
                transaccion.getCategoria(),
                transaccion.getFechaTransaccion(),
                transaccion.getTransaccionRecurrente(),
                transaccion.getFrecuenciaRecurrencia(),
                transaccion.getFechaFinalizacionRecurrencia()
        );
    }

    private UsuariosEntity obtenerUsuarioPorId(Long idUsuario) {
        return authRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario con ID " + idUsuario + " no encontrado"));
    }
}

