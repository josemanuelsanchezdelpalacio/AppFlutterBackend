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
        return metasAhorroRepository.findByUsuarioId(idUsuario)
                .stream()
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

        //calculo la cantidad actual inicial de ingresos
        BigDecimal cantidadActualInicial = calcularCantidadActualInicial(idUsuario, LocalDate.now());
        metaGuardada.setCantidadActual(cantidadActualInicial);
        //verifico si la meta ya esta completada
        metaGuardada.setCompletada(cantidadActualInicial.compareTo(metaGuardada.getCantidadObjetivo()) >= 0);
        //guardo la meta actualizada
        metaGuardada = metasAhorroRepository.save(metaGuardada);

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

        //actualizo los campos de la meta con los valores del DTO
        metaAhorro.setNombre(dto.getNombre());
        metaAhorro.setCategoria(dto.getCategoria());
        metaAhorro.setCantidadObjetivo(dto.getCantidadObjetivo());
        metaAhorro.setCantidadActual(dto.getCantidadActual());
        metaAhorro.setFechaObjetivo(dto.getFechaObjetivo());
        metaAhorro.setCompletada(dto.isCompletada());

        //guardo la meta actualizada
        MetasAhorroEntity metaActualizada = metasAhorroRepository.save(metaAhorro);
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

        //actualizo solo esta meta
        BigDecimal nuevaCantidadActual = meta.getCantidadActual().add(dto.getCantidad());
        meta.setCantidadActual(nuevaCantidadActual);

        //compruebo si se ha alcanzado la meta
        if (nuevaCantidadActual.compareTo(meta.getCantidadObjetivo()) >= 0) {
            meta.setCompletada(true);
        }

        metasAhorroRepository.save(meta);
    }

    //revierto los efectos de una transaccion en las metas de ahorro
    @Transactional
    public void revertirEfectoTransaccion(Long idUsuario, TransaccionesDTO transaccion) {
        //solo revierto efectos si era un ingreso
        if (transaccion.getTipoTransaccion() != TipoTransacciones.INGRESO) {
            return;
        }

        //obtengo todas las metas del usuario
        List<MetasAhorroEntity> metas = metasAhorroRepository.findByUsuarioId(idUsuario);

        //revierto cada meta afectada
        for (MetasAhorroEntity meta : metas) {
            //resto la cantidad de la transaccion a la cantidad actual
            BigDecimal cantidadRevertida = meta.getCantidadActual().subtract(transaccion.getCantidad());
            meta.setCantidadActual(cantidadRevertida);

            //si estaba completada, verifico si ya no lo esta
            if (meta.isCompletada() && cantidadRevertida.compareTo(meta.getCantidadObjetivo()) < 0) {
                meta.setCompletada(false);
            }

            //guardo la meta actualizada
            metasAhorroRepository.save(meta);
        }
    }
}

