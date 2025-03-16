package com.iessanalberto.jms.backendapp.controller;

import com.iessanalberto.jms.backendapp.DTO.AuthDTO.*;
import com.iessanalberto.jms.backendapp.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDTO> registrarUsuario(@RequestBody RegisterRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.registrarUsuario(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDTO(false, null, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUsuario(@RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.autenticarUsuario(request.getEmail(), request.getPassword()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, null, e.getMessage(), null));
        }
    }

    @PostMapping("/loginGoogle")
    public ResponseEntity<AuthResponseDTO> googleAuth(@RequestBody GoogleAuthRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.autenticarUsuarioGoogle(request.getEmail(), request.getIdUsuarioFirebase()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, null, e.getMessage(), null));
        }
    }

    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<AuthResponseDTO> solicitarRecuperacion(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            return ResponseEntity.ok(authService.solicitarRecuperacionContrasenia(email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponseDTO(false, null, e.getMessage(), null));
        }
    }
}

