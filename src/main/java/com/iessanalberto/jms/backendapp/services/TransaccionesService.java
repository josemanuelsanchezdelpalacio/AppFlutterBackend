package com.iessanalberto.jms.backendapp.services;

import org.springframework.beans.factory.annotation.Value;
import com.iessanalberto.jms.backendapp.entities.TransaccionesEntity;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import com.iessanalberto.jms.backendapp.repository.TransaccionesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransaccionesService {

    @Value("${file.upload-dir}")
    private String directorioSubida;

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

    //metodo para obtener las transacciones del usuario a traves de su id
    public List<TransaccionesDTO> obtenerTransacciones(Long idUsuario) {
        return transaccionesRepository.buscarPorUsuarioId(idUsuario)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    //metodo para obtener las transacciones por fecha del usuario a traves de su id
    public List<TransaccionesDTO> obtenerTransaccionesFecha(Long idUsuario, LocalDate inicioFecha, LocalDate finFecha) {
        return transaccionesRepository.buscarTransaccionPorFecha(idUsuario, inicioFecha, finFecha)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    //metodo para crear una transaccion
    @Transactional
    public TransaccionesDTO crearTransacciones(Long idUsuario, TransaccionesDTO dto, MultipartFile imagen) throws IOException {
        UsuariosEntity usuario = obtenerUsuarioPorId(idUsuario);

        TransaccionesEntity transaccion = new TransaccionesEntity();
        transaccion.setUsuario(usuario);
        transaccion.setCantidad(dto.getCantidad());
        transaccion.setDescripcion(dto.getDescripcion());
        transaccion.setTipo(dto.getTipoTransaccion());
        transaccion.setCategoria(dto.getCategoria());
        transaccion.setFechaTransaccion(dto.getFechaTransaccion());
        transaccion.setFechaCreacion(LocalDateTime.now());
        transaccion.setTransaccionRecurrente(dto.getTransaccionRecurrente());
        transaccion.setFrecuenciaRecurrencia(dto.getFrecuenciaRecurrencia());
        transaccion.setFechaFinalizacionRecurrencia(dto.getFechaFinalizacionRecurrencia());

        if (imagen != null && !imagen.isEmpty()) {
            String imagenUrl = guardarImagen(imagen);
            transaccion.setImagenUrl(imagenUrl);
        }

        TransaccionesEntity transaccionGuardada = transaccionesRepository.save(transaccion);
        return convertirDTO(transaccionGuardada);
    }

    //metodo para actualizar una transaccion
    @Transactional
    public TransaccionesDTO actualizarTransacciones(Long idUsuario, Long idTransaccion, TransaccionesDTO dto) {
        TransaccionesEntity transaccion = transaccionesRepository.findById(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (transaccion.getUsuario().getId() != idUsuario) {
            throw new SecurityException("No autorizado para actualizar esta transacción");
        }

        //obtener la transaccion original para revertir sus efectos
        TransaccionesDTO transaccionOriginal = convertirDTO(transaccion);

        //revierto efectos de la transaccion original
        revertirEfectosTransaccion(idUsuario, transaccionOriginal);

        //actualizo la entidad con los nuevos valores
        transaccion.setCantidad(dto.getCantidad());
        transaccion.setDescripcion(dto.getDescripcion());
        transaccion.setTipo(dto.getTipoTransaccion());
        transaccion.setCategoria(dto.getCategoria());
        transaccion.setFechaTransaccion(dto.getFechaTransaccion());
        transaccion.setTransaccionRecurrente(dto.getTransaccionRecurrente());
        transaccion.setFrecuenciaRecurrencia(dto.getFrecuenciaRecurrencia());
        transaccion.setFechaFinalizacionRecurrencia(dto.getFechaFinalizacionRecurrencia());

        //actualizo la transaccion
        TransaccionesEntity transaccionActualizada = transaccionesRepository.save(transaccion);

        //aplico los efectos de la transaccion actualizada
        actualizarMetasYPresupuestos(idUsuario, dto);

        return convertirDTO(transaccionActualizada);
    }

    //metodo para borrar transacciones
    @Transactional
    public void borrarTransacciones(Long idUsuario, Long idTransaccion) {
        TransaccionesEntity transaccion = transaccionesRepository.findById(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (transaccion.getUsuario().getId() != idUsuario) {
            throw new RuntimeException("No autorizado para eliminar esta transacción");
        }

        //obtengo la transaccion antes de eliminarla
        TransaccionesDTO transaccionAEliminar = convertirDTO(transaccion);

        //revierto los efectos de la transaccion
        revertirEfectosTransaccion(idUsuario, transaccionAEliminar);

        //elimino la transaccion
        transaccionesRepository.delete(transaccion);
    }

    private void actualizarMetasYPresupuestos(Long idUsuario, TransaccionesDTO dto) {
        //actualizar metas de ahorro
        metasAhorroService.actualizarMetasPorTransaccion(idUsuario, dto);

        //actualizar presupuestos
        presupuestosService.actualizarPresupuestosPorTransaccion(idUsuario, dto);
    }

    private void revertirEfectosTransaccion(Long idUsuario, TransaccionesDTO dto) {
        //revertir efectos en metas de ahorro
        metasAhorroService.revertirEfectoTransaccion(idUsuario, dto);

        //revertir efectos en presupuestos
        presupuestosService.revertirEfectoTransaccion(idUsuario, dto);
    }

    private TransaccionesDTO convertirDTO(TransaccionesEntity transaccion) {
        return new TransaccionesDTO(
                transaccion.getId(),
                transaccion.getNombre(),
                transaccion.getCantidad(),
                transaccion.getDescripcion(),
                transaccion.getTipo(),
                transaccion.getCategoria(),
                transaccion.getFechaTransaccion(),
                transaccion.getTransaccionRecurrente(),
                transaccion.getFrecuenciaRecurrencia(),
                transaccion.getFechaFinalizacionRecurrencia(),
                transaccion.getImagenUrl()
        );
    }

    private UsuariosEntity obtenerUsuarioPorId(Long idUsuario) {
        return authRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario con ID " + idUsuario + " no encontrado"));
    }

    //metodo para asegurar que existe el directorio de subida
    private void asegurarDirectorioSubidaExiste() throws IOException {
        Path rutaDirectorio = Paths.get(directorioSubida);
        if (!Files.exists(rutaDirectorio)) {
            Files.createDirectories(rutaDirectorio);
            System.out.println("Directorio de subida de imagenes creado en: " + rutaDirectorio.toAbsolutePath());
        }
    }

    //metodo para guardar la imagen
    private String guardarImagen(MultipartFile imagen) throws IOException {
        asegurarDirectorioSubidaExiste();

        String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
        Path rutaArchivo = Paths.get(directorioSubida).resolve(nombreArchivo).normalize();
        System.out.println("Guardando imagen en: " + rutaArchivo.toAbsolutePath());

        Files.copy(imagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }
}

