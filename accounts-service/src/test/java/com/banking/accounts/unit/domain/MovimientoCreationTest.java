package com.banking.accounts.unit.domain;

import com.banking.accounts.domain.exception.JustificacionRequeridaException;
import com.banking.accounts.domain.exception.ValorMovimientoInvalidoException;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoMovimiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoCreationTest {

    @Test
    void ajusteWithoutJustificacionShouldThrowException() {
        assertThrows(JustificacionRequeridaException.class, () ->
                Movimiento.create(TipoMovimiento.AJUSTE, new BigDecimal("100.00"),
                        new BigDecimal("600.00"), 1L, null, null)
        );
    }

    @Test
    void ajusteWithValidJustificacionShouldCreateInstance() {
        Movimiento movimiento = Movimiento.create(TipoMovimiento.AJUSTE, new BigDecimal("100.00"),
                new BigDecimal("600.00"), 1L, null, "Corrección de saldo");
        assertNotNull(movimiento);
        assertEquals(TipoMovimiento.AJUSTE, movimiento.getTipoMovimiento());
        assertEquals("Corrección de saldo", movimiento.getJustificacion());
    }

    @Test
    void depositoWithNullJustificacionShouldBeValid() {
        Movimiento movimiento = Movimiento.create(TipoMovimiento.DEPOSITO, new BigDecimal("200.00"),
                new BigDecimal("700.00"), 1L, null, null);
        assertNotNull(movimiento);
        assertNull(movimiento.getJustificacion());
    }

    @Test
    void movimientoWithZeroValueShouldThrowException() {
        assertThrows(ValorMovimientoInvalidoException.class, () ->
                Movimiento.create(TipoMovimiento.DEPOSITO, BigDecimal.ZERO,
                        new BigDecimal("500.00"), 1L, null, null)
        );
    }

    @Test
    void reversionWithNullJustificacionShouldBeValid() {
        Movimiento movimiento = Movimiento.create(TipoMovimiento.REVERSION, new BigDecimal("100.00"),
                new BigDecimal("400.00"), 1L, 5L, null);
        assertNotNull(movimiento);
        assertNull(movimiento.getJustificacion());
    }
}
