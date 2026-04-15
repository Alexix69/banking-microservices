package com.banking.accounts.application.dto;

import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;

import java.math.BigDecimal;

public class CuentaResponse {

    private final Long id;
    private final String numeroCuenta;
    private final TipoCuenta tipo;
    private final BigDecimal saldoInicial;
    private final BigDecimal saldoDisponible;
    private final EstadoCuenta estado;
    private final Long clienteId;

    public CuentaResponse(Long id, String numeroCuenta, TipoCuenta tipo, BigDecimal saldoInicial,
                          BigDecimal saldoDisponible, EstadoCuenta estado, Long clienteId) {
        this.id = id;
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.saldoDisponible = saldoDisponible;
        this.estado = estado;
        this.clienteId = clienteId;
    }

    public static CuentaResponse from(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipo(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldoDisponible(),
                cuenta.getEstado(),
                cuenta.getClienteId()
        );
    }

    public Long getId() {
        return id;
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

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public Long getClienteId() {
        return clienteId;
    }
}
