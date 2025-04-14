package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.DTO.PresupuestosDTO.PresupuestosDTO;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TipoTransacciones;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.entities.TransaccionesEntity;
import com.iessanalberto.jms.backendapp.entities.PresupuestosEntity;
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

    //metodo para inicializar un presupuesto calculando los gastos actuales y el saldo restante
    private void inicializarPresupuesto(PresupuestosEntity presupuesto) {
        //calculo la cantidad gastada hasta el momento en esta categoria
        BigDecimal cantidadGastada = calcularCantidadGastada(presupuesto);
        presupuesto.setCantidadGastada(cantidadGastada);
        //calculo la cantidad restante restando lo gastado del total
        presupuesto.setCantidadRestante(presupuesto.getCantidad().subtract(cantidadGastada));
    }

    //metodo para obtener todos los presupuestos de un usuario
    public List<PresupuestosDTO> obtenerPresupuesto(Long idUsuario) {
        try {
            //busco todos los presupuestos del usuario
            List<PresupuestosEntity> presupuestos = presupuestosRepository.findByUsuarioId(idUsuario);

            //convierto cada presupuesto a DTO y los devuelvo como lista
            return presupuestos.stream()
                    .map(this::convertirDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener presupuestos: " + e.getMessage());
        }
    }

    //metodo para crear un nuevo presupuesto para un usuario
    public PresupuestosDTO crearPresupuesto(Long idUsuario, PresupuestosDTO dto) {

        //obtengo la entidad del usuario
        UsuariosEntity usuario = obtenerUsuarioPorId(idUsuario);

        //creo un nuevo presupuesto con los datos del DTO
        PresupuestosEntity presupuesto = new PresupuestosEntity();
        presupuesto.setUsuario(usuario);
        presupuesto.setNombre(dto.getNombre());
        presupuesto.setCategoria(dto.getCategoria());
        presupuesto.setCantidad(dto.getCantidad());
        presupuesto.setFechaInicio(dto.getFechaInicio());
        presupuesto.setFechaFin(dto.getFechaFin());
        presupuesto.setFechaCreacion(LocalDateTime.now());

        //inicializo los valores de gasto y saldo restante
        inicializarPresupuesto(presupuesto);

        //guardo y devuelvo el presupuesto como DTO
        return convertirDTO(presupuestosRepository.save(presupuesto));
    }

    //metodo para actualizar un presupuesto existente
    @Transactional
    public PresupuestosDTO actualizarPresupuesto(Long idUsuario, Long idPresupuesto, PresupuestosDTO dto) {

        //busco el presupuesto por su ID
        PresupuestosEntity presupuesto = presupuestosRepository.findById(idPresupuesto)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        //compruebo que el presupuesto pertenezca al usuario
        if (presupuesto.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("No autorizado para actualizar el presupuesto");
        }

        //actualizo los campos del presupuesto con los valores del DTO
        presupuesto.setNombre(dto.getNombre());
        presupuesto.setCategoria(dto.getCategoria());
        presupuesto.setCantidad(dto.getCantidad());
        presupuesto.setFechaInicio(dto.getFechaInicio());
        presupuesto.setFechaFin(dto.getFechaFin());

        //recalculo los gastos y el saldo restante
        inicializarPresupuesto(presupuesto);

        //guardo y devuelvo el presupuesto actualizado como DTO
        return convertirDTO(presupuestosRepository.save(presupuesto));
    }

    //metodo para eliminar un presupuesto
    @Transactional
    public void borrarPresupuesto(Long idUsuario, Long idPresupuesto) {

        //busco el presupuesto por su ID
        PresupuestosEntity presupuesto = presupuestosRepository.findById(idPresupuesto)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        //compruebo que el presupuesto pertenezca al usuario
        if (presupuesto.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("No autorizado para eliminar el presupuesto");
        }

        //elimino el presupuesto
        presupuestosRepository.delete(presupuesto);
    }

    //metodo para actualizar los presupuestos afectados por una nueva transaccion
    @Transactional
    public void actualizarPresupuestosPorTransaccion(Long idUsuario, TransaccionesDTO dto) {
        if (dto.getTipoTransaccion() != TipoTransacciones.GASTO || dto.getPresupuestoId() == null) {
            return;
        }

        //busco solo el presupuesto especifico al que esta asignada la transaccion
        PresupuestosEntity presupuesto = presupuestosRepository.findById(dto.getPresupuestoId())
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        //compruebo que el presupuesto pertenece al usuario
        if (presupuesto.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("Presupuesto no pertenece al usuario");
        }

        //actualizo solo este presupuesto
        BigDecimal nuevaCantidadGastada = presupuesto.getCantidadGastada().add(dto.getCantidad());
        presupuesto.setCantidadGastada(nuevaCantidadGastada);

        BigDecimal nuevaCantidadRestante = presupuesto.getCantidad().subtract(nuevaCantidadGastada);
        presupuesto.setCantidadRestante(nuevaCantidadRestante.compareTo(BigDecimal.ZERO) < 0 ?
                BigDecimal.ZERO : nuevaCantidadRestante);

        presupuestosRepository.save(presupuesto);
    }

    //metodo para calcular la cantidad total gastada para un presupuesto
    private BigDecimal calcularCantidadGastada(PresupuestosEntity presupuesto) {
        try {
            LocalDate inicioFecha = presupuesto.getFechaInicio();
            LocalDate fechaFin = presupuesto.getFechaFin();

            //busco todas las transacciones en el periodo del presupuesto
            return transaccionesRepository.buscarTransaccionPorFecha(
                            presupuesto.getUsuario().getId(), inicioFecha, fechaFin)
                    .stream()

                    //filtro solo los gastos de la categoria del presupuesto
                    .filter(t -> t.getTipo() == TipoTransacciones.GASTO &&
                            t.getCategoria().equals(presupuesto.getCategoria()))
                    //extraigo la cantidad de cada transaccion
                    .map(TransaccionesEntity::getCantidad)
                    //sumo todas las cantidades
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    //metodo para convertir una entidad de presupuesto a DTO
    private PresupuestosDTO convertirDTO(PresupuestosEntity presupuesto) {
        try {
            //calculo valores actualizados
            BigDecimal cantidadGastada = calcularCantidadGastada(presupuesto);
            BigDecimal cantidadRestante = presupuesto.getCantidad().subtract(cantidadGastada);

            //aseguro que la cantidad restante no sea negativa
            if (cantidadRestante.compareTo(BigDecimal.ZERO) < 0) {
                cantidadRestante = BigDecimal.ZERO;
            }

            //creo y devuelvo el DTO con todos los datos
            return new PresupuestosDTO(
                    presupuesto.getId(),
                    presupuesto.getNombre(),
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

    //obtengo un usuario por su ID
    private UsuariosEntity obtenerUsuarioPorId(Long idUsuario) {
        return authRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario con ID " + idUsuario + " no encontrado"));
    }

    //revierto los efectos de una transaccion (para actualizaciones o eliminaciones)
    @Transactional
    public void revertirEfectoTransaccion(Long idUsuario, TransaccionesDTO dto) {
        if (dto.getTipoTransaccion() != TipoTransacciones.GASTO) {
            return;
        }

        // Si tiene un presupuesto específico asignado, solo revertimos ese
        if (dto.getPresupuestoId() != null) {
            PresupuestosEntity presupuesto = presupuestosRepository.findById(dto.getPresupuestoId())
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

            if (presupuesto.getUsuario().getId() != idUsuario) {
                throw new RuntimeException("Presupuesto no pertenece al usuario");
            }

            BigDecimal cantidadRevertida = presupuesto.getCantidadGastada().subtract(dto.getCantidad());
            presupuesto.setCantidadGastada(cantidadRevertida.compareTo(BigDecimal.ZERO) < 0 ?
                    BigDecimal.ZERO : cantidadRevertida);

            BigDecimal nuevaCantidadRestante = presupuesto.getCantidad().subtract(presupuesto.getCantidadGastada());
            presupuesto.setCantidadRestante(nuevaCantidadRestante.compareTo(BigDecimal.ZERO) < 0 ?
                    BigDecimal.ZERO : nuevaCantidadRestante);

            presupuestosRepository.save(presupuesto);
        }
    }
}

