package com.banking.accounts.application.dto;

import jakarta.validation.constraints.NotNull;

public class CrearReversionRequest {

    @NotNull
    private final Long movimientoOrigenId;

    public CrearReversionRequest(Long movimientoOrigenId) {
        this.movimientoOrigenId = movimientoOrigenId;
    }

    public Long getMovimientoOrigenId() {
        return movimientoOrigenId;
    }
}
