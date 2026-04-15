package com.banking.accounts.unit.domain;

import com.banking.accounts.domain.exception.CuentaInactivaException;
import com.banking.accounts.domain.exception.LimiteDiarioExcedidoException;
import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.validator.CuentaActivaValidator;
import com.banking.accounts.domain.validator.LimiteDiarioValidator;
import com.banking.accounts.domain.validator.MovimientoValidator;
import com.banking.accounts.domain.validator.SaldoInsuficienteValidator;
import com.banking.accounts.domain.validator.ValorCeroValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoValidatorTest {

    private Cuenta cuentaActiva(BigDecimal saldo) {
        return Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                saldo, saldo, EstadoCuenta.ACTIVA, 1L);
    }

    private Cuenta cuentaInactiva() {
        return Cuenta.reconstitute(2L, "ACC-002", TipoCuenta.AHORRO,
                new BigDecimal("500.00"), new BigDecimal("500.00"),
                EstadoCuenta.INACTIVA, 1L);
    }

    @Test
    void valorCeroValidatorShouldThrowForZeroValue() {
        ValorCeroValidator validator = new ValorCeroValidator();
        assertThrows(RuntimeException.class, () ->
                validator.validar(cuentaActiva(new BigDecimal("500.00")),
                        BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }

    @Test
    void cuentaActivaValidatorShouldThrowForInactiveCuenta() {
        CuentaActivaValidator validator = new CuentaActivaValidator();
        assertThrows(CuentaInactivaException.class, () ->
                validator.validar(cuentaInactiva(), new BigDecimal("-100.00"), BigDecimal.ZERO)
        );
    }

    @Test
    void saldoInsuficienteValidatorShouldThrowWhenBalanceLow() {
        SaldoInsuficienteValidator validator = new SaldoInsuficienteValidator();
        assertThrows(SaldoInsuficienteException.class, () ->
                validator.validar(cuentaActiva(new BigDecimal("50.00")),
                        new BigDecimal("-100.00"), BigDecimal.ZERO)
        );
    }

    @Test
    void saldoInsuficienteValidatorShouldNotActForDeposit() {
        SaldoInsuficienteValidator validator = new SaldoInsuficienteValidator();
        assertDoesNotThrow(() ->
                validator.validar(cuentaActiva(new BigDecimal("50.00")),
                        new BigDecimal("100.00"), BigDecimal.ZERO)
        );
    }

    @Test
    void limiteDiarioValidatorShouldThrowWhenLimitExceeded() {
        LimiteDiarioValidator validator = new LimiteDiarioValidator();
        assertThrows(LimiteDiarioExcedidoException.class, () ->
                validator.validar(cuentaActiva(new BigDecimal("1000.00")),
                        new BigDecimal("-300.00"), new BigDecimal("300.00"))
        );
    }

    @Test
    void limiteDiarioValidatorShouldNotActForDeposit() {
        LimiteDiarioValidator validator = new LimiteDiarioValidator();
        assertDoesNotThrow(() ->
                validator.validar(cuentaActiva(new BigDecimal("1000.00")),
                        new BigDecimal("300.00"), new BigDecimal("400.00"))
        );
    }

    @Test
    void limiteDiarioValidatorShouldAccumulateCorrectly() {
        LimiteDiarioValidator validator = new LimiteDiarioValidator();
        assertDoesNotThrow(() ->
                validator.validar(cuentaActiva(new BigDecimal("1000.00")),
                        new BigDecimal("-200.00"), new BigDecimal("299.00"))
        );
        assertThrows(LimiteDiarioExcedidoException.class, () ->
                validator.validar(cuentaActiva(new BigDecimal("1000.00")),
                        new BigDecimal("-200.00"), new BigDecimal("301.00"))
        );
    }

    @Test
    void chainOf4ValidatorsShouldExecuteInOrder() {
        List<MovimientoValidator> chain = List.of(
                new ValorCeroValidator(),
                new CuentaActivaValidator(),
                new SaldoInsuficienteValidator(),
                new LimiteDiarioValidator()
        );
        Cuenta cuenta = cuentaActiva(new BigDecimal("500.00"));
        BigDecimal valor = new BigDecimal("-100.00");
        BigDecimal acumulado = BigDecimal.ZERO;
        assertDoesNotThrow(() -> chain.forEach(v -> v.validar(cuenta, valor, acumulado)));
    }
}
