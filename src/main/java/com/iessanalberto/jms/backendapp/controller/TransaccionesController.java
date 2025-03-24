package com.iessanalberto.jms.backendapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.services.TransaccionesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionesController {

    private final TransaccionesService transaccionesService;
    private final ObjectMapper objectMapper;

    //constructor para inicializar servicios y mapeador de objetos
    public TransaccionesController(TransaccionesService transaccionesService, ObjectMapper objectMapper) {
        this.transaccionesService = transaccionesService;
        this.objectMapper = objectMapper;
    }

    //obtengo lista de transacciones de un usuario
    @GetMapping("/obtenerTransacciones")
    public ResponseEntity<List<TransaccionesDTO>> obtenerTransacciones(@RequestParam Long idUsuario) {
        List<TransaccionesDTO> transacciones = transaccionesService.obtenerTransacciones(idUsuario);
        return ResponseEntity.ok(transacciones);
    }

    //obtengo lista de transacciones dentro de un rango de fechas
    @GetMapping("/transacciones-rango-fechas")
    public ResponseEntity<List<TransaccionesDTO>> obtenerTransaccionesPorFecha(
            @RequestParam Long idUsuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<TransaccionesDTO> transacciones = transaccionesService.obtenerTransaccionesFecha(idUsuario, fechaInicio, fechaFin);
        return ResponseEntity.ok(transacciones);
    }

    //creo una nueva transacción con datos y una imagen opcional
    @PostMapping("/crearTransacciones")
    public ResponseEntity<TransaccionesDTO> crearTransacciones(
            @RequestParam Long idUsuario,
            @RequestParam("transaccion") String transaccionJson,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        try {
            //convierto el JSON recibido en un objeto DTO
            TransaccionesDTO transaccionesDTO = objectMapper.readValue(transaccionJson, TransaccionesDTO.class);
            TransaccionesDTO createdTransaccion = transaccionesService.crearTransacciones(idUsuario, transaccionesDTO, imagen);
            return ResponseEntity.ok(createdTransaccion);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    //actualizo una transacción existente
    @PutMapping("/actualizarTransacciones")
    public ResponseEntity<TransaccionesDTO> actualizarTransacciones(
            @RequestParam Long idUsuario,
            @RequestParam Long idTransaccion,
            @RequestBody TransaccionesDTO transaccionesDTO) {
        TransaccionesDTO updatedTransaccion = transaccionesService.actualizarTransacciones(idUsuario, idTransaccion, transaccionesDTO);
        return ResponseEntity.ok(updatedTransaccion);
    }

    //elimino una transacción
    @DeleteMapping("/borrarTransacciones")
    public ResponseEntity<Void> borrarTransacciones(
            @RequestParam Long idUsuario,
            @RequestParam Long idTransaccion) {
        transaccionesService.borrarTransacciones(idUsuario, idTransaccion);
        return ResponseEntity.noContent().build();
    }
}


