package com.iessanalberto.jms.backendapp.DTO.MetasAhorroDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetasAhorroDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private BigDecimal cantidadObjetivo;
    private BigDecimal cantidadActual;
    private LocalDate fechaObjetivo;
    private boolean completada;
}


