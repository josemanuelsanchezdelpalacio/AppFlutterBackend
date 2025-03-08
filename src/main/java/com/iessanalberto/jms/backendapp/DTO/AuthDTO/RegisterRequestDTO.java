package com.iessanalberto.jms.backendapp.DTO.AuthDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequestDTO {
    private String email;
    private String password;
    private AuthProvider authProvider;
    private String idUsuarioFirebase;
}

