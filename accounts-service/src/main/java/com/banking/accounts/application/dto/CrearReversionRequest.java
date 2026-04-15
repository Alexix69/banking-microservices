package com.banking.accounts.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class CrearReversionRequest {

    @NotNull
    private final Long movimientoOrigenId;

    @JsonCreator
    public CrearReversionRequest(@JsonProperty("movimientoOrigenId") Long movimientoOrigenId) {
        this.movimientoOrigenId = movimientoOrigenId;
    }

    public Long getMovimientoOrigenId() {
        return movimientoOrigenId;
    }
}
