package com.banking.accounts.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CrearAjusteRequest {

    @NotNull
    private final Long movimientoOrigenId;

    @NotNull
    private final BigDecimal valor;

    @NotBlank
    @Size(max = 500)
    private final String justificacion;

    public CrearAjusteRequest(Long movimientoOrigenId, BigDecimal valor, String justificacion) {
        this.movimientoOrigenId = movimientoOrigenId;
        this.valor = valor;
        this.justificacion = justificacion;
    }

    public Long getMovimientoOrigenId() {
        return movimientoOrigenId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getJustificacion() {
        return justificacion;
    }
}
