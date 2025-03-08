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

    public PresupuestosController(PresupuestosService presupuestosService) {
        this.presupuestosService = presupuestosService;
    }

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

    @GetMapping("/obtenerPresupuesto")
    public ResponseEntity<?> obtenerPresupuesto(@RequestParam Long idUsuario) {
        try {
            List<PresupuestosDTO> presupuestos = presupuestosService.obtenerPresupuesto(idUsuario);
            return ResponseEntity.ok(presupuestos);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al obtener presupuestos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/actualizarPresupuesto")
    public ResponseEntity<PresupuestosDTO> actualizarPresupuesto(
            @RequestParam Long idPresupuesto,
            @RequestParam Long idUsuario,
            @RequestBody PresupuestosDTO dto
    ) {
        return ResponseEntity.ok(presupuestosService.actualizarPresupuesto(idUsuario, idPresupuesto, dto));
    }

    @DeleteMapping("/borrarPresupuesto")
    public ResponseEntity<Void> borrarPresupuesto(
            @RequestParam Long idPresupuesto,
            @RequestParam Long idUsuario
    ) {
        presupuestosService.borrarPresupuesto(idUsuario, idPresupuesto);
        return ResponseEntity.noContent().build();
    }
}


