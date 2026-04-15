package com.banking.accounts.application.dto;

import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoResponse {

    private final Long id;
    private final LocalDateTime fecha;
    private final TipoMovimiento tipoMovimiento;
    private final BigDecimal valor;
    private final BigDecimal saldoResultante;
    private final Long cuentaId;
    private final Long movimientoOrigenId;
    private final String justificacion;

    public MovimientoResponse(Long id, LocalDateTime fecha, TipoMovimiento tipoMovimiento,
                              BigDecimal valor, BigDecimal saldoResultante,
                              Long cuentaId, Long movimientoOrigenId, String justificacion) {
        this.id = id;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.valor = valor;
        this.saldoResultante = saldoResultante;
        this.cuentaId = cuentaId;
        this.movimientoOrigenId = movimientoOrigenId;
        this.justificacion = justificacion;
    }

    public static MovimientoResponse from(Movimiento movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldoResultante(),
                movimiento.getCuentaId(),
                movimiento.getMovimientoOrigenId(),
                movimiento.getJustificacion()
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getSaldoResultante() {
        return saldoResultante;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public Long getMovimientoOrigenId() {
        return movimientoOrigenId;
    }

    public String getJustificacion() {
        return justificacion;
    }
}
