package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.DTO.MetasAhorroDTO.MetasAhorroDTO;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TipoTransacciones;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.entities.MetasAhorroEntity;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import com.iessanalberto.jms.backendapp.repository.MetasAhorroRepository;
import com.iessanalberto.jms.backendapp.repository.TransaccionesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetasAhorroService {

    private final TransaccionesRepository transaccionesRepository;
    private final MetasAhorroRepository metasAhorroRepository;
    private final AuthRepository usuariosRepository;

    public MetasAhorroService(TransaccionesRepository transaccionesRepository,
                              MetasAhorroRepository metasAhorroRepository,
                              AuthRepository usuariosRepository) {
        this.metasAhorroRepository = metasAhorroRepository;
        this.usuariosRepository = usuariosRepository;
        this.transaccionesRepository = transaccionesRepository;
    }

    private BigDecimal calcularCantidadActualInicial(Long idUsuario, LocalDate fechaCreacion) {
        return transaccionesRepository.buscarTransaccionPorFecha(idUsuario, fechaCreacion, LocalDate.now())
                .stream()
                .filter(t -> t.getTipo() == TipoTransacciones.INGRESO)
                .map(t -> t.getCantidad())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private MetasAhorroDTO convertirDTO(MetasAhorroEntity meta) {
        return new MetasAhorroDTO(
                meta.getId(),
                meta.getNombre(),
                meta.getCategoria(), // Added categoria to DTO conversion
                meta.getCantidadObjetivo(),
                meta.getCantidadActual(),
                meta.getFechaObjetivo(),
                meta.isCompletada()
        );
    }

    public List<MetasAhorroDTO> obtenerMetasAhorro(Long idUsuario) {
        return metasAhorroRepository.findByUsuarioId(idUsuario)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    public MetasAhorroDTO crearMetasAhorro(Long idUsuario, MetasAhorroDTO dto) {
        UsuariosEntity usuario = usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MetasAhorroEntity metaAhorro = new MetasAhorroEntity();
        metaAhorro.setUsuario(usuario);
        metaAhorro.setNombre(dto.getNombre());
        metaAhorro.setCategoria(dto.getCategoria()); // Added categoria
        metaAhorro.setCantidadObjetivo(dto.getCantidadObjetivo());
        metaAhorro.setCantidadActual(dto.getCantidadActual());
        metaAhorro.setFechaObjetivo(dto.getFechaObjetivo());
        metaAhorro.setCompletada(dto.isCompletada());
        metaAhorro.setFechaCreacion(LocalDateTime.now());

        MetasAhorroEntity metaGuardada = metasAhorroRepository.save(metaAhorro);

        BigDecimal cantidadActualInicial = calcularCantidadActualInicial(idUsuario, LocalDate.now());
        metaAhorro.setCantidadActual(cantidadActualInicial);
        metaAhorro.setCompletada(cantidadActualInicial.compareTo(metaAhorro.getCantidadObjetivo()) >= 0);

        return convertirDTO(metaGuardada);
    }

    public MetasAhorroDTO actualizarMetasAhorro(Long idUsuario, Long idMetaAhorro, MetasAhorroDTO dto) {
        MetasAhorroEntity metaAhorro = metasAhorroRepository.findById(idMetaAhorro)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        if (!metaAhorro.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para actualizar la meta de ahorro");
        }

        metaAhorro.setNombre(dto.getNombre());
        metaAhorro.setCategoria(dto.getCategoria()); // Added categoria update
        metaAhorro.setCantidadObjetivo(dto.getCantidadObjetivo());
        metaAhorro.setCantidadActual(dto.getCantidadActual());
        metaAhorro.setFechaObjetivo(dto.getFechaObjetivo());
        metaAhorro.setCompletada(dto.isCompletada());

        MetasAhorroEntity metaActualizada = metasAhorroRepository.save(metaAhorro);
        return convertirDTO(metaActualizada);
    }

    @Transactional
    public void borrarMetasAhorro(Long idUsuario, Long idMetaAhorro) {
        MetasAhorroEntity metaAhorro = metasAhorroRepository.findById(idMetaAhorro)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        if (!metaAhorro.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("No autorizado para eliminar esta meta");
        }

        metasAhorroRepository.delete(metaAhorro);
    }

    @Transactional
    public void actualizarMetasPorTransaccion(Long idUsuario, TransaccionesDTO transaccion) {
        // Solo actualizar metas si es un ingreso
        if (transaccion.getTipoTransaccion() != TipoTransacciones.INGRESO) {
            return;
        }

        List<MetasAhorroEntity> metas = metasAhorroRepository.findByUsuarioIdAndCompletadaFalse(idUsuario);

        for (MetasAhorroEntity meta : metas) {
            // Actualizar cantidad actual
            BigDecimal nuevaCantidadActual = meta.getCantidadActual().add(transaccion.getCantidad());
            meta.setCantidadActual(nuevaCantidadActual);

            // Verificar si se ha alcanzado la meta
            if (nuevaCantidadActual.compareTo(meta.getCantidadObjetivo()) >= 0) {
                meta.setCompletada(true);
            }

            metasAhorroRepository.save(meta);
        }
    }

    @Transactional
    public void revertirEfectoTransaccion(Long idUsuario, TransaccionesDTO transaccion) {
        // Solo revertir efectos si era un ingreso
        if (transaccion.getTipoTransaccion() != TipoTransacciones.INGRESO) {
            return;
        }

        List<MetasAhorroEntity> metas = metasAhorroRepository.findByUsuarioId(idUsuario);

        for (MetasAhorroEntity meta : metas) {
            // Revertir la cantidad
            BigDecimal cantidadRevertida = meta.getCantidadActual().subtract(transaccion.getCantidad());
            meta.setCantidadActual(cantidadRevertida);

            // Si estaba completada, verificar si ya no lo está
            if (meta.isCompletada() && cantidadRevertida.compareTo(meta.getCantidadObjetivo()) < 0) {
                meta.setCompletada(false);
            }

            metasAhorroRepository.save(meta);
        }
    }
}

