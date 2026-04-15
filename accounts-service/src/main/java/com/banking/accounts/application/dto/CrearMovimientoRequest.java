package com.banking.accounts.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CrearMovimientoRequest {

    @NotNull
    private final Long cuentaId;

    @NotNull
    @Digits(integer = 13, fraction = 2)
    private final BigDecimal valor;

    public CrearMovimientoRequest(Long cuentaId, BigDecimal valor) {
        this.cuentaId = cuentaId;
        this.valor = valor;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
