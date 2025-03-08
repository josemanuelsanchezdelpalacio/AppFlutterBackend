package com.iessanalberto.jms.backendapp.DTO.AuthDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponseDTO {
    private boolean success;
    private String email;
    private String mensaje;
    private Long idUsuario;

    public AuthResponseDTO(boolean success, String email, String mensaje, Long idUsuario) {
        this.success = success;
        this.email = email;
        this.mensaje = mensaje;
        this.idUsuario = idUsuario;
    }
}

