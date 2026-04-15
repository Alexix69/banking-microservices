package com.banking.accounts.application.dto;

import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReporteItemResponse {

    private final LocalDateTime fecha;
    private final String cliente;
    private final String numeroCuenta;
    private final TipoCuenta tipoCuenta;
    private final BigDecimal saldoInicial;
    private final EstadoCuenta estado;
    private final BigDecimal movimiento;
    private final BigDecimal saldoDisponible;

    public ReporteItemResponse(LocalDateTime fecha, String cliente, String numeroCuenta,
                               TipoCuenta tipoCuenta, BigDecimal saldoInicial,
                               EstadoCuenta estado, BigDecimal movimiento,
                               BigDecimal saldoDisponible) {
        this.fecha = fecha;
        this.cliente = cliente;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = saldoInicial;
        this.estado = estado;
        this.movimiento = movimiento;
        this.saldoDisponible = saldoDisponible;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public BigDecimal getMovimiento() {
        return movimiento;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }
}
