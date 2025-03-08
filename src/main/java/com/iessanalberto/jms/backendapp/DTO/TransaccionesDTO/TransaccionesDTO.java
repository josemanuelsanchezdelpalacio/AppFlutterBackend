package com.iessanalberto.jms.backendapp.DTO.TransaccionesDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionesDTO {
    private Long id;
    private BigDecimal cantidad;
    private String descripcion;
    private TipoTransacciones tipoTransaccion;
    private String categoria;
    private LocalDate fechaTransaccion;
    private Boolean transaccionRecurrente;
    private String frecuenciaRecurrencia;
    private LocalDateTime fechaFinalizacionRecurrencia;
}


