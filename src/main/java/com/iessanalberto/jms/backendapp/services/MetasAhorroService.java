package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.entities.MetasAhorroEntity;
import com.iessanalberto.jms.backendapp.entities.TransaccionesEntity;
import com.iessanalberto.jms.backendapp.DTO.MetasAhorroDTO.MetasAhorroDTO;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TipoTransacciones;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import com.iessanalberto.jms.backendapp.repository.MetasAhorroRepository;
import com.iessanalberto.jms.backendapp.repository.TransaccionesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetasAhorroService {

    private static final Logger logger = LoggerFactory.getLogger(MetasAhorroService.class);

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

    //calculo la cantidad actual inicial de ingresos para un usuario desde una fecha dada
    private BigDecimal calcularCantidadActualInicial(Long idUsuario, LocalDate fechaCreacion) {
        return transaccionesRepository.buscarTransaccionPorFecha(idUsuario, fechaCreacion, LocalDate.now())
                .stream()
                //filtro solo los ingresos
                .filter(t -> t.getTipo() == TipoTransacciones.INGRESO)
                //extraigo la cantidad de cada transaccion
                .map(TransaccionesEntity::getCantidad)
                //sumo todas las cantidades
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Calcula la cantidad actual acumulada de una meta específica basada en transacciones asociadas
    private BigDecimal calcularCantidadActualMeta(Long idUsuario, Long idMeta) {
        return transaccionesRepository.buscarPorUsuarioId(idUsuario)
                .stream()
                // Filtro solo ingresos asociados a esta meta específica
                .filter(t -> t.getTipo() == TipoTransacciones.INGRESO &&
                        idMeta.equals(t.getMetaAhorroId()))
                // Extraigo la cantidad de cada transaccion
                .map(TransaccionesEntity::getCantidad)
                // Sumo todas las cantidades
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //convierto una entidad de meta de ahorro a DTO
    private MetasAhorroDTO convertirDTO(MetasAhorroEntity meta) {
        return new MetasAhorroDTO(
                meta.getId(),
                meta.getNombre(),
                meta.getCategoria(),
                meta.getCantidadObjetivo(),
                meta.getCantidadActual(),
                meta.getFechaObjetivo(),
                meta.isCompletada()
        );
    }

    //obtengo todas las metas de ahorro de un usuario
    public List<MetasAhorroDTO> obtenerMetasAhorro(Long idUsuario) {
        List<MetasAhorroEntity> metas = metasAhorroRepository.findByUsuarioId(idUsuario);

        // Actualizar cada meta con la cantidad actual calculada desde las transacciones
        metas.forEach(meta -> {
            // Recalcular cantidad actual basada en transacciones asociadas
            BigDecimal cantidadActual = calcularCantidadActualMeta(idUsuario, meta.getId());
            meta.setCantidadActual(cantidadActual);

            // Actualizar estado completado
            boolean completada = cantidadActual.compareTo(meta.getCantidadObjetivo()) >= 0;
            meta.setCompletada(completada);

            // Guardar cambios
            metasAhorroRepository.save(meta);
        });

        return metas.stream()
                //convierto cada entidad a DTO
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    //creo una nueva meta de ahorro para un usuario
    public MetasAhorroDTO crearMetasAhorro(Long idUsuario, MetasAhorroDTO dto) {
        //obtengo el usuario por su ID
        UsuariosEntity usuario = usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //creo una nueva entidad de meta de ahorro con los datos del DTO
        MetasAhorroEntity metaAhorro = new MetasAhorroEntity();
        metaAhorro.setUsuario(usuario);
        metaAhorro.setNombre(dto.getNombre());
        metaAhorro.setCategoria(dto.getCategoria());
        metaAhorro.setCantidadObjetivo(dto.getCantidadObjetivo());
        metaAhorro.setCantidadActual(BigDecimal.ZERO); //inicializo en cero
        metaAhorro.setFechaObjetivo(dto.getFechaObjetivo());
        metaAhorro.setCompletada(false);
        metaAhorro.setFechaCreacion(LocalDateTime.now());

        //guardo la meta en la base de datos
        MetasAhorroEntity metaGuardada = metasAhorroRepository.save(metaAhorro);

        logger.info("Meta de ahorro creada con ID: {}", metaGuardada.getId());

        //devuelvo la meta como DTO
        return convertirDTO(metaGuardada);
    }

    //actualizo una meta de ahorro existente
    public MetasAhorroDTO actualizarMetasAhorro(Long idUsuario, Long idMetaAhorro, MetasAhorroDTO dto) {
        //busco la meta por su ID
        MetasAhorroEntity metaAhorro = metasAhorroRepository.findById(idMetaAhorro)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        //compruebo que la meta pertenezca al usuario
        if (metaAhorro.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("No autorizado para actualizar la meta de ahorro");
        }

        logger.info("Actualizando meta de ahorro ID: {} - Valores anteriores: objetivo={}, actual={}, completada={}",
                idMetaAhorro, metaAhorro.getCantidadObjetivo(), metaAhorro.getCantidadActual(), metaAhorro.isCompletada());

        //actualizo los campos de la meta con los valores del DTO
        metaAhorro.setNombre(dto.getNombre());
        metaAhorro.setCategoria(dto.getCategoria());
        metaAhorro.setCantidadObjetivo(dto.getCantidadObjetivo());
        metaAhorro.setCantidadActual(dto.getCantidadActual());
        metaAhorro.setFechaObjetivo(dto.getFechaObjetivo());
        metaAhorro.setCompletada(dto.getCantidadActual().compareTo(dto.getCantidadObjetivo()) >= 0);

        //guardo la meta actualizada
        MetasAhorroEntity metaActualizada = metasAhorroRepository.save(metaAhorro);

        logger.info("Meta de ahorro actualizada - Nuevos valores: objetivo={}, actual={}, completada={}",
                metaActualizada.getCantidadObjetivo(), metaActualizada.getCantidadActual(), metaActualizada.isCompletada());

        //devuelvo la meta como DTO
        return convertirDTO(metaActualizada);
    }

    //elimino una meta de ahorro
    @Transactional
    public void borrarMetasAhorro(Long idUsuario, Long idMetaAhorro) {
        //busco la meta por su ID
        MetasAhorroEntity metaAhorro = metasAhorroRepository.findById(idMetaAhorro)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        //compruebo que la meta pertenezca al usuario
        if (metaAhorro.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("No autorizado para eliminar esta meta");
        }

        logger.info("Eliminando meta de ahorro ID: {}", idMetaAhorro);

        //elimino la meta
        metasAhorroRepository.delete(metaAhorro);
    }

    //actualizo las metas de ahorro afectadas por una nueva transaccion
    @Transactional
    public void actualizarMetasPorTransaccion(Long idUsuario, TransaccionesDTO dto) {
        if (dto.getTipoTransaccion() != TipoTransacciones.INGRESO || dto.getMetaAhorroId() == null) {
            return;
        }

        //busco solo la meta especifica a la que esta asignada la transaccion
        MetasAhorroEntity meta = metasAhorroRepository.findById(dto.getMetaAhorroId())
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        //compruebo que la meta pertenece al usuario
        if (meta.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("Meta no pertenece al usuario");
        }

        logger.info("Actualizando meta ID: {} por transacción. Cantidad actual: {}, Añadiendo: {}",
                meta.getId(), meta.getCantidadActual(), dto.getCantidad());

        //actualizo solo esta meta
        BigDecimal nuevaCantidadActual = meta.getCantidadActual().add(dto.getCantidad());
        meta.setCantidadActual(nuevaCantidadActual);

        //compruebo si se ha alcanzado la meta
        boolean completada = nuevaCantidadActual.compareTo(meta.getCantidadObjetivo()) >= 0;
        meta.setCompletada(completada);

        metasAhorroRepository.save(meta);

        logger.info("Meta actualizada. Nueva cantidad: {}, Completada: {}",
                nuevaCantidadActual, completada);
    }

    //revierto los efectos de una transaccion en las metas de ahorro
    @Transactional
    public void revertirEfectoTransaccion(Long idUsuario, TransaccionesDTO transaccion) {
        // Solo revierto efectos si era un ingreso asociado a una meta
        if (transaccion.getTipoTransaccion() != TipoTransacciones.INGRESO ||
                transaccion.getMetaAhorroId() == null) {
            logger.debug("No se revierte efecto - No es ingreso o no tiene meta asociada");
            return;
        }

        logger.info("Iniciando reversión de transacción: ID={}, Cantidad={}, MetaID={}",
                transaccion.getId(), transaccion.getCantidad(), transaccion.getMetaAhorroId());

        MetasAhorroEntity meta = metasAhorroRepository.findById(transaccion.getMetaAhorroId())
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        if (meta.getUsuario().getId() != idUsuario) {
            logger.error("Error de seguridad: Meta {} no pertenece al usuario {}",
                    meta.getId(), idUsuario);
            throw new RuntimeException("Meta no pertenece al usuario");
        }

        // Guardar valores anteriores para logging
        BigDecimal cantidadAnterior = meta.getCantidadActual();
        boolean completadaAnterior = meta.isCompletada();

        logger.info("Estado actual antes de revertir - Meta ID: {}, Cantidad: {}, Completada: {}",
                meta.getId(), cantidadAnterior, completadaAnterior);

        // Revertir la cantidad
        BigDecimal cantidadRevertida = cantidadAnterior.subtract(transaccion.getCantidad());
        // Asegurar que no sea negativa
        if (cantidadRevertida.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Cantidad revertida sería negativa, estableciendo a CERO");
            cantidadRevertida = BigDecimal.ZERO;
        }

        meta.setCantidadActual(cantidadRevertida);

        // Actualizar estado completado según la nueva cantidad
        boolean nuevoEstadoCompletado = cantidadRevertida.compareTo(meta.getCantidadObjetivo()) >= 0;
        meta.setCompletada(nuevoEstadoCompletado);

        // Guardar cambios
        metasAhorroRepository.save(meta);

        logger.info("Transacción revertida con éxito - Meta ID: {}, Nueva cantidad: {}, Nuevo estado completado: {}",
                meta.getId(), cantidadRevertida, nuevoEstadoCompletado);
        logger.info("Cambios realizados: Cantidad {} -> {}, Completada {} -> {}",
                cantidadAnterior, cantidadRevertida, completadaAnterior, nuevoEstadoCompletado);
    }

    // Método para recalcular completamente una meta específica (útil para sincronización)
    @Transactional
    public void recalcularMeta(Long idUsuario, Long idMeta) {
        MetasAhorroEntity meta = metasAhorroRepository.findById(idMeta)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        if (meta.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("Meta no pertenece al usuario");
        }

        // Recalcular cantidad actual basada en transacciones asociadas
        BigDecimal cantidadCalculada = calcularCantidadActualMeta(idUsuario, idMeta);
        meta.setCantidadActual(cantidadCalculada);

        // Actualizar estado completado
        meta.setCompletada(cantidadCalculada.compareTo(meta.getCantidadObjetivo()) >= 0);

        // Guardar cambios
        metasAhorroRepository.save(meta);

        logger.info("Meta recalculada: ID={}, CantidadActual={}, Completada={}",
                meta.getId(), meta.getCantidadActual(), meta.isCompletada());
    }
}


