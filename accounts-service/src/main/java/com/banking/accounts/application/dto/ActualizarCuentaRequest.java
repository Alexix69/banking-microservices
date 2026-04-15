package com.banking.accounts.application.dto;

import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;

import java.math.BigDecimal;

public class ActualizarCuentaRequest {

    private final String numeroCuenta;
    private final TipoCuenta tipo;
    private final BigDecimal saldoInicial;
    private final EstadoCuenta estado;
    private final Long clienteId;

    public ActualizarCuentaRequest(String numeroCuenta, TipoCuenta tipo, BigDecimal saldoInicial,
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
