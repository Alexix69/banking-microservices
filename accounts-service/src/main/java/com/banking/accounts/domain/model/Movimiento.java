package com.banking.accounts.domain.model;

import com.banking.accounts.domain.exception.JustificacionRequeridaException;
import com.banking.accounts.domain.exception.ValorMovimientoInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Movimiento {

    private Long id;
    private LocalDateTime fecha;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldoResultante;
    private Long cuentaId;
    private Long movimientoOrigenId;
    private String justificacion;

    private Movimiento() {
    }

    public static Movimiento create(TipoMovimiento tipoMovimiento, BigDecimal valor,
                                    BigDecimal saldoResultante, Long cuentaId,
                                    Long movimientoOrigenId, String justificacion) {
        if (valor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValorMovimientoInvalidoException();
        }
        if (tipoMovimiento == TipoMovimiento.AJUSTE
                && (justificacion == null || justificacion.isBlank())) {
            throw new JustificacionRequeridaException();
        }
        Movimiento movimiento = new Movimiento();
        movimiento.fecha = LocalDateTime.now();
        movimiento.tipoMovimiento = tipoMovimiento;
        movimiento.valor = valor;
        movimiento.saldoResultante = saldoResultante;
        movimiento.cuentaId = cuentaId;
        movimiento.movimientoOrigenId = movimientoOrigenId;
        movimiento.justificacion = justificacion;
        return movimiento;
    }

    public static Movimiento reconstitute(Long id, LocalDateTime fecha,
                                          TipoMovimiento tipoMovimiento, BigDecimal valor,
                                          BigDecimal saldoResultante, Long cuentaId,
                                          Long movimientoOrigenId, String justificacion) {
        Movimiento movimiento = new Movimiento();
        movimiento.id = id;
        movimiento.fecha = fecha;
        movimiento.tipoMovimiento = tipoMovimiento;
        movimiento.valor = valor;
        movimiento.saldoResultante = saldoResultante;
        movimiento.cuentaId = cuentaId;
        movimiento.movimientoOrigenId = movimientoOrigenId;
        movimiento.justificacion = justificacion;
        return movimiento;
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
