package com.iessanalberto.jms.backendapp.DTO.AuthDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleAuthRequestDTO {
    private String email;
    private String idUsuarioFirebase;
}

