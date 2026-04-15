package com.banking.accounts.application.dto;

import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CrearCuentaRequest {

    @NotBlank
    private final String numeroCuenta;

    @NotNull
    private final TipoCuenta tipo;

    @NotNull
    @DecimalMin("0")
    private final BigDecimal saldoInicial;

    @NotNull
    private final EstadoCuenta estado;

    @NotNull
    private final Long clienteId;

    public CrearCuentaRequest(String numeroCuenta, TipoCuenta tipo, BigDecimal saldoInicial,
                              EstadoCuenta estado, Long clienteId) {
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.estado = estado;
        this.clienteId = clienteId;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public Long getClienteId() {
        return clienteId;
    }
}
