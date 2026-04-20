package com.banking.accounts.unit.domain;

import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaInvariantTest {

    @Test
    void aplicarMovimientoThatLeavesSaldoNegativeShouldThrow() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("100"), new BigDecimal("100"), EstadoCuenta.ACTIVA, 1L);

        assertThrows(SaldoInsuficienteException.class, () ->
                cuenta.aplicarMovimiento(new BigDecimal("-200")));

        assertEquals(0, new BigDecimal("100").compareTo(cuenta.getSaldoDisponible()));
    }

    @Test
    void aplicarMovimientoExactlyZeroBalanceShouldBeValid() {
        Cuenta cuenta = Cuenta.reconstitute(2L, "ACC-002", TipoCuenta.AHORRO,
                new BigDecimal("100"), new BigDecimal("100"), EstadoCuenta.ACTIVA, 1L);

        assertDoesNotThrow(() -> cuenta.aplicarMovimiento(new BigDecimal("-100")));

        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getSaldoDisponible()));
    }

    @Test
    void aplicarMovimientoPositiveShouldAlwaysBeValid() {
        Cuenta cuenta = Cuenta.reconstitute(3L, "ACC-003", TipoCuenta.AHORRO,
                new BigDecimal("0"), new BigDecimal("0"), EstadoCuenta.ACTIVA, 1L);

        assertDoesNotThrow(() -> cuenta.aplicarMovimiento(new BigDecimal("500")));

        assertEquals(0, new BigDecimal("500").compareTo(cuenta.getSaldoDisponible()));
    }

    @Test
    void aplicarMovimientoSequenceThatWouldGoNegativeShouldThrow() {
        Cuenta cuenta = Cuenta.reconstitute(4L, "ACC-004", TipoCuenta.AHORRO,
                new BigDecimal("100"), new BigDecimal("100"), EstadoCuenta.ACTIVA, 1L);

        assertDoesNotThrow(() -> cuenta.aplicarMovimiento(new BigDecimal("-60")));
        assertEquals(0, new BigDecimal("40").compareTo(cuenta.getSaldoDisponible()));

        assertThrows(SaldoInsuficienteException.class, () ->
                cuenta.aplicarMovimiento(new BigDecimal("-60")));

        assertEquals(0, new BigDecimal("40").compareTo(cuenta.getSaldoDisponible()));
    }
}
