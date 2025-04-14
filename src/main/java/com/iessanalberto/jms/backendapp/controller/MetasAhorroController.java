package com.iessanalberto.jms.backendapp.controller;

import com.iessanalberto.jms.backendapp.DTO.MetasAhorroDTO.MetasAhorroDTO;
import com.iessanalberto.jms.backendapp.services.MetasAhorroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas-ahorro")
public class MetasAhorroController {

    private final MetasAhorroService metasAhorroService;

    //constructor para inicializar el servicio de metas de ahorro
    public MetasAhorroController(MetasAhorroService metasAhorroService) {
        this.metasAhorroService = metasAhorroService;
    }

    //creo una nueva meta de ahorro para un usuario
    @PostMapping("/crear")
    public ResponseEntity<Object> crearMetasAhorro(
            @RequestParam(name = "idUsuario") Long idUsuario,
            @RequestBody MetasAhorroDTO dto
    ) {
        try {
            MetasAhorroDTO nuevaMeta = metasAhorroService.crearMetasAhorro(idUsuario, dto);
            return ResponseEntity.ok(nuevaMeta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear la meta de ahorro: " + e.getMessage());
        }
    }

    //obtengo la lista de metas de ahorro de un usuario
    @GetMapping("/listar")
    public ResponseEntity<List<MetasAhorroDTO>> obtenerMetasAhorro(@RequestParam Long idUsuario) {
        try {
            List<MetasAhorroDTO> metas = metasAhorroService.obtenerMetasAhorro(idUsuario);
            return ResponseEntity.ok(metas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    //actualizo una meta de ahorro existente
    @PutMapping("/actualizar")
    public ResponseEntity<Object> actualizarMetasAhorro(
            @RequestParam Long idMetaAhorro,
            @RequestParam Long idUsuario,
            @RequestBody MetasAhorroDTO dto
    ) {
        try {
            MetasAhorroDTO metaActualizada = metasAhorroService.actualizarMetasAhorro(idUsuario, idMetaAhorro, dto);
            return ResponseEntity.ok(metaActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al actualizar la meta de ahorro: " + e.getMessage());
        }
    }

    //elimino una meta de ahorro existente
    @DeleteMapping("/eliminar")
    public ResponseEntity<Object> borrarMetasAhorro(
            @RequestParam Long idMetaAhorro,
            @RequestParam Long idUsuario
    ) {
        try {
            metasAhorroService.borrarMetasAhorro(idUsuario, idMetaAhorro);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


