package com.iessanalberto.jms.backendapp.DTO.PresupuestosDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestosDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private BigDecimal cantidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal cantidadGastada;
    private BigDecimal cantidadRestante;
}

