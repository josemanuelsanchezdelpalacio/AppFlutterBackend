package com.iessanalberto.jms.backendapp.controller;

import com.iessanalberto.jms.backendapp.DTO.PresupuestosDTO.PresupuestosDTO;
import com.iessanalberto.jms.backendapp.services.PresupuestosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestosController {

    private final PresupuestosService presupuestosService;

    //constructor para inicializar el servicio de presupuestos
    public PresupuestosController(PresupuestosService presupuestosService) {
        this.presupuestosService = presupuestosService;
    }

    //creo un nuevo presupuesto para un usuario
    @PostMapping("/crearPresupuesto")
    public ResponseEntity<PresupuestosDTO> crearPresupuesto(
            @RequestParam Long idUsuario,
            @RequestBody PresupuestosDTO dto
    ) {
        try {
            PresupuestosDTO result = presupuestosService.crearPresupuesto(idUsuario, dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw e;
        }
    }

    //obtengo la lista de presupuestos de un usuario
    @GetMapping("/obtenerPresupuesto")
    public ResponseEntity<?> obtenerPresupuesto(@RequestParam Long idUsuario) {
        try {
            List<PresupuestosDTO> presupuestos = presupuestosService.obtenerPresupuesto(idUsuario);
            return ResponseEntity.ok(presupuestos);
        } catch (Exception e) {
            //manejo el error devolviendo un mensaje adecuado
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al obtener presupuestos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //actualizo un presupuesto existente
    @PutMapping("/actualizarPresupuesto")
    public ResponseEntity<?> actualizarPresupuesto(
            @RequestParam Long idPresupuesto,
            @RequestParam Long idUsuario,
            @RequestBody PresupuestosDTO dto
    ) {
        try {
            PresupuestosDTO result = presupuestosService.actualizarPresupuesto(idUsuario, idPresupuesto, dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al actualizar presupuesto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //elimino un presupuesto existente
    @DeleteMapping("/borrarPresupuesto")
    public ResponseEntity<Void> borrarPresupuesto(
            @RequestParam Long idPresupuesto,
            @RequestParam Long idUsuario
    ) {
        presupuestosService.borrarPresupuesto(idUsuario, idPresupuesto);
        return ResponseEntity.noContent().build();
    }
}

