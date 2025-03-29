package com.iessanalberto.jms.backendapp.services;

import com.iessanalberto.jms.backendapp.DTO.AuthDTO.*;
import com.iessanalberto.jms.backendapp.entities.UsuariosEntity;
import com.iessanalberto.jms.backendapp.repository.AuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    //constructor para inicializar repositorio y codificador de contraseñas
    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //registro de usuario con autenticación local o Google
    public AuthResponseDTO registrarUsuario(RegisterRequestDTO request) {
        //verifico si el email es valido
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email es requerido");
        }

        //busco si el usuario ya existe en la base de datos
        Optional<UsuariosEntity> existingUser = authRepository.buscarPorEmail(request.getEmail());

        //verifico si el usuario usa autenticación de Google
        if (request.getAuthProvider() == AuthProvider.GOOGLE) {
            //verifico si el id de firebase esta presente
            if (request.getIdUsuarioFirebase() == null || request.getIdUsuarioFirebase().trim().isEmpty()) {
                throw new RuntimeException("El id de Firebase es necesario para registro con Google");
            }

            //obtengo usuario existente o creo uno nuevo
            UsuariosEntity usuario = existingUser.orElseGet(UsuariosEntity::new);
            usuario.setEmail(request.getEmail());
            usuario.setAuthProvider(AuthProvider.GOOGLE);
            usuario.setIdUsuarioFirebase(request.getIdUsuarioFirebase());
            usuario = authRepository.save(usuario);

            return new AuthResponseDTO(true, usuario.getEmail(), "Usuario registrado correctamente", usuario.getId());
        }

        //verifico si el usuario ya existe para evitar registros duplicados
        if (existingUser.isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        //verifico si la contraseña es valida
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña es requerida para registro local");
        }

        //creo un nuevo usuario con autenticación local
        UsuariosEntity usuario = new UsuariosEntity();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setAuthProvider(AuthProvider.LOCAL);
        usuario = authRepository.save(usuario);

        return new AuthResponseDTO(true, usuario.getEmail(), "Usuario registrado correctamente", usuario.getId());
    }

    //autenticacion de usuario con email y contraseña
    public AuthResponseDTO autenticarUsuario(String email, String password) {
        //busco usuario en la base de datos
        UsuariosEntity user = authRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //verifico si el usuario debe autenticarse con Google
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new RuntimeException("Este usuario debe iniciar sesión con Google");
        }

        //verifico si la contraseña es correcta
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return new AuthResponseDTO(true, user.getEmail(), "Login exitoso", user.getId());
    }

    //autenticacion de usuario con Google
    public AuthResponseDTO autenticarUsuarioGoogle(String email, String idUsuarioFirebase) {
        //busco usuario o lo creo si no existe
        UsuariosEntity user = authRepository.buscarPorEmail(email)
                .orElseGet(() -> {
                    UsuariosEntity newUser = new UsuariosEntity();
                    newUser.setEmail(email);
                    newUser.setAuthProvider(AuthProvider.GOOGLE);
                    newUser.setIdUsuarioFirebase(idUsuarioFirebase);
                    return authRepository.save(newUser);
                });

        //actualizo el id de firebase si ha cambiado
        if (!idUsuarioFirebase.equals(user.getIdUsuarioFirebase())) {
            user.setIdUsuarioFirebase(idUsuarioFirebase);
            user = authRepository.save(user);
        }

        return new AuthResponseDTO(true, user.getEmail(), "Login con Google correcto", user.getId());
    }

    //solicitud de recuperacion de contraseña
    public AuthResponseDTO solicitarRecuperacionContrasenia(String email) {
        // Validación más estricta del email
        if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("El email proporcionado no es válido");
        }

        //busco al usuario en la base de datos
        UsuariosEntity usuario = authRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario registrado con este email"));

        //compruebo que el usuario sea local
        if (usuario.getAuthProvider() != AuthProvider.LOCAL) {
            throw new RuntimeException("Este usuario no puede restablecer su contraseña ya que usa " +
                    usuario.getAuthProvider().toString() + ". Por favor, inicie sesión con ese método.");
        }

        //compruebo que el usuario tenga una contraseña establecida
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            throw new RuntimeException("Este usuario no tiene una contraseña establecida");
        }

        return new AuthResponseDTO(
                true,
                usuario.getEmail(),
                "Se ha enviado un enlace de recuperación a tu correo electrónico.",
                usuario.getId()
        );
    }
}


