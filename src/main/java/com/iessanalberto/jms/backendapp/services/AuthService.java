package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.DTO.AuthDTO.*;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO registrarUsuario(RegisterRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email es requerido");
        }

        Optional<UsuariosEntity> existingUser = authRepository.buscarPorEmail(request.getEmail());

        if (request.getAuthProvider() == AuthProvider.GOOGLE) {
            if (request.getIdUsuarioFirebase() == null || request.getIdUsuarioFirebase().trim().isEmpty()) {
                throw new RuntimeException("El id de Firebase es necesario para registro con Google");
            }

            UsuariosEntity usuario = existingUser.orElseGet(UsuariosEntity::new);
            usuario.setEmail(request.getEmail());
            usuario.setAuthProvider(AuthProvider.GOOGLE);
            usuario.setIdUsuarioFirebase(request.getIdUsuarioFirebase());
            usuario = authRepository.save(usuario);

            return new AuthResponseDTO(true, usuario.getEmail(), "Usuario registrado correctamente", usuario.getId());
        }

        if (existingUser.isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña es requerida para registro local");
        }

        UsuariosEntity usuario = new UsuariosEntity();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setAuthProvider(AuthProvider.LOCAL);
        usuario = authRepository.save(usuario);

        return new AuthResponseDTO(true, usuario.getEmail(), "Usuario registrado correctamente", usuario.getId());
    }

    public AuthResponseDTO autenticarUsuario(String email, String password) {
        UsuariosEntity user = authRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new RuntimeException("Este usuario debe iniciar sesión con Google");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return new AuthResponseDTO(true, user.getEmail(), "Login con Google correcto", user.getId());
    }

    public AuthResponseDTO autenticarUsuarioGoogle(String email, String idUsuarioFirebase) {
        UsuariosEntity user = authRepository.buscarPorEmail(email)
                .orElseGet(() -> {
                    UsuariosEntity newUser = new UsuariosEntity();
                    newUser.setEmail(email);
                    newUser.setAuthProvider(AuthProvider.GOOGLE);
                    newUser.setIdUsuarioFirebase(idUsuarioFirebase);
                    return authRepository.save(newUser);
                });

        if (!idUsuarioFirebase.equals(user.getIdUsuarioFirebase())) {
            user.setIdUsuarioFirebase(idUsuarioFirebase);
            user = authRepository.save(user);
        }

        return new AuthResponseDTO(true, user.getEmail(), "Login con Google correcto", user.getId());
    }

    public AuthResponseDTO solicitarRecuperacionContrasenia(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El email es requerido");
        }

        UsuariosEntity usuario = authRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getAuthProvider() != AuthProvider.LOCAL) {
            throw new RuntimeException("Este usuario no puede restablecer su contraseña ya que usa " +
                    usuario.getAuthProvider().toString());
        }

        // No need to send email here as the Flutter client will handle it with Firebase
        return new AuthResponseDTO(true, usuario.getEmail(),
                "Solicitud de recuperación de contraseña aprobada", usuario.getId());
    }
}

