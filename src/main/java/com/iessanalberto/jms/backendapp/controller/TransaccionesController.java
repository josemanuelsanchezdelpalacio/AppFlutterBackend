package com.iessanalberto.jms.backendapp.controller;

import com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO.TransaccionesDTO;
import com.iessanalberto.jms.backendapp.services.TransaccionesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionesController {

    private final TransaccionesService transaccionesService;

    public TransaccionesController(TransaccionesService transaccionesService) {
        this.transaccionesService = transaccionesService;
    }

    @PostMapping("/crearTransacciones")
    public ResponseEntity<TransaccionesDTO> crearTransacciones(@RequestParam Long idUsuario, @RequestBody TransaccionesDTO dto) {
        try {
            TransaccionesDTO nuevaTransaccion = transaccionesService.crearTransacciones(idUsuario, dto);
            return ResponseEntity.ok(nuevaTransaccion);
        } catch (Exception e) {
            System.err.println("Error al crear la transacción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/obtenerTransacciones")
    public ResponseEntity<List<TransaccionesDTO>> obtenerTransacciones(@RequestParam Long idUsuario) {
        return ResponseEntity.ok(transaccionesService.obtenerTransacciones(idUsuario));
    }

    @GetMapping("/transacciones-rango-fechas")
    public ResponseEntity<List<TransaccionesDTO>> obtenerTransaccionesFecha(
            @RequestParam Long idUsuario,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin
    ) {
        try {
            List<TransaccionesDTO> transacciones = transaccionesService.obtenerTransaccionesFecha(
                    idUsuario,
                    fechaInicio,
                    fechaFin
            );
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }
    }

    @PutMapping("/actualizarTransacciones")
    public ResponseEntity<TransaccionesDTO> actualizarTransacciones(
            @RequestParam Long idUsuario,
            @RequestParam Long idTransaccion,
            @RequestBody TransaccionesDTO dto
    ) {
        TransaccionesDTO updatedTransaction = transaccionesService.actualizarTransacciones(idUsuario, idTransaccion, dto);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/borrarTransacciones")
    public ResponseEntity<Void> borrarTransacciones(@RequestParam Long idUsuario, @RequestParam Long idTransaccion) {
        transaccionesService.borrarTransacciones(idUsuario, idTransaccion);
        return ResponseEntity.noContent().build();
    }
}



