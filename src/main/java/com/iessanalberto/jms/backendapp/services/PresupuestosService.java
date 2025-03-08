package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.DTO.PresupuestosDTO.PresupuestosDTO;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TipoTransacciones;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.entities.PresupuestosEntity;
import com.iessanalberto.jms.backendapp.entities.TransaccionesEntity;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import com.iessanalberto.jms.backendapp.repository.PresupuestosRepository;
import com.iessanalberto.jms.backendapp.repository.TransaccionesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestosService {

    private final PresupuestosRepository presupuestosRepository;
    private final TransaccionesRepository transaccionesRepository;
    private final AuthRepository authRepository;

    public PresupuestosService(PresupuestosRepository presupuestosRepository,
                               TransaccionesRepository transaccionesRepository,
                               AuthRepository authRepository) {
        this.presupuestosRepository = presupuestosRepository;
        this.transaccionesRepository = transaccionesRepository;
        this.authRepository = authRepository;
    }

    private void inicializarPresupuesto(PresupuestosEntity presupuesto) {
        BigDecimal cantidadGastada = calcularCantidadGastada(presupuesto);
        presupuesto.setCantidadGastada(cantidadGastada);
        presupuesto.setCantidadRestante(presupuesto.getCantidad().subtract(cantidadGastada));
    }

    public List<PresupuestosDTO> obtenerPresupuesto(Long idUsuario) {
        try {
            List<PresupuestosEntity> presupuestos = presupuestosRepository.findByUsuarioId(idUsuario);

            return presupuestos.stream()
                    .map(this::convertirDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener presupuestos: " + e.getMessage());
        }
    }

    public PresupuestosDTO crearPresupuesto(Long idUsuario, PresupuestosDTO dto) {
        UsuariosEntity usuario = obtenerUsuarioPorId(idUsuario);

        PresupuestosEntity presupuesto = new PresupuestosEntity();
        presupuesto.setUsuario(usuario);
        presupuesto.setNombre(dto.getNombre()); // Added nombre
        presupuesto.setCategoria(dto.getCategoria());
        presupuesto.setCantidad(dto.getCantidad());
        presupuesto.setFechaInicio(dto.getFechaInicio());
        presupuesto.setFechaFin(dto.getFechaFin());
        presupuesto.setFechaCreacion(LocalDateTime.now());

        inicializarPresupuesto(presupuesto);
        return convertirDTO(presupuestosRepository.save(presupuesto));
    }

    @Transactional
    public PresupuestosDTO actualizarPresupuesto(Long idUsuario, Long idPresupuesto, PresupuestosDTO dto) {
        PresupuestosEntity presupuesto = presupuestosRepository.findById(idPresupuesto)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        if (!presupuesto.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para actualizar el presupuesto");
        }

        presupuesto.setNombre(dto.getNombre()); // Added nombre
        presupuesto.setCategoria(dto.getCategoria());
        presupuesto.setCantidad(dto.getCantidad());
        presupuesto.setFechaInicio(dto.getFechaInicio());
        presupuesto.setFechaFin(dto.getFechaFin());

        return convertirDTO(presupuestosRepository.save(presupuesto));
    }

    @Transactional
    public void borrarPresupuesto(Long idUsuario, Long idPresupuesto) {
        PresupuestosEntity presupuesto = presupuestosRepository.findById(idPresupuesto)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        if (!presupuesto.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para eliminar el presupuesto");
        }

        presupuestosRepository.delete(presupuesto);
    }

    @Transactional
    public void actualizarPresupuestosPorTransaccion(Long idUsuario, TransaccionesDTO dto) {
        if (dto.getTipoTransaccion() != TipoTransacciones.GASTO) {
            return;
        }

        List<PresupuestosEntity> presupuestos = presupuestosRepository.findByUsuarioId(idUsuario);

        for (PresupuestosEntity presupuesto : presupuestos) {
            if (dto.getCategoria().equals(presupuesto.getCategoria()) &&
                    !dto.getFechaTransaccion().isBefore(presupuesto.getFechaInicio()) &&
                    !dto.getFechaTransaccion().isAfter(presupuesto.getFechaFin())) {
                PresupuestosDTO presupuestoDTO = convertirDTO(presupuesto);
                actualizarPresupuesto(idUsuario, presupuesto.getId(), presupuestoDTO);
            }
        }
    }

    private BigDecimal calcularCantidadGastada(PresupuestosEntity presupuesto) {
        try {
            LocalDate inicioFecha = presupuesto.getFechaInicio();
            LocalDate fechaFin = presupuesto.getFechaFin();

            return transaccionesRepository.buscarTransaccionPorFecha(
                            presupuesto.getUsuario().getId(), inicioFecha, fechaFin)
                    .stream()
                    .filter(t -> t.getTipo() == TipoTransacciones.GASTO &&
                            t.getCategoria().equals(presupuesto.getCategoria()))
                    .map(TransaccionesEntity::getCantidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private PresupuestosDTO convertirDTO(PresupuestosEntity presupuesto) {
        try {
            BigDecimal cantidadGastada = calcularCantidadGastada(presupuesto);
            BigDecimal cantidadRestante = presupuesto.getCantidad().subtract(cantidadGastada);

            // Asegurar que la cantidad restante no sea negativa
            if (cantidadRestante.compareTo(BigDecimal.ZERO) < 0) {
                cantidadRestante = BigDecimal.ZERO;
            }

            return new PresupuestosDTO(
                    presupuesto.getId(),
                    presupuesto.getNombre(), // Added nombre
                    presupuesto.getCategoria(),
                    presupuesto.getCantidad(),
                    presupuesto.getFechaInicio(),
                    presupuesto.getFechaFin(),
                    cantidadGastada,
                    cantidadRestante
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir presupuesto a DTO: " + e.getMessage());
        }
    }

    private UsuariosEntity obtenerUsuarioPorId(Long idUsuario) {
        return authRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario con ID " + idUsuario + " no encontrado"));
    }

    // Método para revertir los efectos de una transacción (útil para actualizaciones o eliminaciones)
    @Transactional
    public void revertirEfectoTransaccion(Long idUsuario, TransaccionesDTO dto) {
        if (dto.getTipoTransaccion() != TipoTransacciones.GASTO) {
            return;
        }

        List<PresupuestosEntity> presupuestos = presupuestosRepository.findByUsuarioId(idUsuario)
                .stream()
                .filter(p -> p.getCategoria().equals(dto.getCategoria()) &&
                        !dto.getFechaTransaccion().isBefore(p.getFechaInicio()) &&
                        !dto.getFechaTransaccion().isAfter(p.getFechaFin()))
                .collect(Collectors.toList());

        for (PresupuestosEntity presupuesto : presupuestos) {
            PresupuestosDTO presupuestoDTO = convertirDTO(presupuesto);
            actualizarPresupuesto(idUsuario, presupuesto.getId(), presupuestoDTO);
        }
    }
}



