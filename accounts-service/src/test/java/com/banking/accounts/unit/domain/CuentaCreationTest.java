package com.banking.accounts.unit.domain;

import com.banking.accounts.domain.exception.ClienteInactivoException;
import com.banking.accounts.domain.exception.SaldoInicialInvalidoException;
import com.banking.accounts.domain.exception.SaldoMinimoInsuficienteException;
import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.EstadoCliente;
import com.banking.accounts.domain.model.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaCreationTest {

    private ClienteProyeccion clienteActivo() {
        return ClienteProyeccion.create(1L, "Juan Perez");
    }

    private ClienteProyeccion clienteInactivo() {
        return ClienteProyeccion.reconstitute(2L, "Maria Lopez", EstadoCliente.INACTIVO);
    }

    @Test
    void cuentaAhorroWithZeroBalanceShouldBeValid() {
        Cuenta cuenta = Cuenta.create("ACC-001", TipoCuenta.AHORRO, BigDecimal.ZERO,
                EstadoCuenta.ACTIVA, clienteActivo());
        assertNotNull(cuenta);
        assertEquals(BigDecimal.ZERO, cuenta.getSaldoInicial());
        assertEquals(BigDecimal.ZERO, cuenta.getSaldoDisponible());
    }

    @Test
    void cuentaCorrienteWithBalanceLessThan50ShouldFail() {
        assertThrows(SaldoMinimoInsuficienteException.class, () ->
                Cuenta.create("ACC-002", TipoCuenta.CORRIENTE, new BigDecimal("49.99"),
                        EstadoCuenta.ACTIVA, clienteActivo())
        );
    }

    @Test
    void cuentaWithNegativeBalanceShouldFail() {
        assertThrows(SaldoInicialInvalidoException.class, () ->
                Cuenta.create("ACC-003", TipoCuenta.AHORRO, new BigDecimal("-1.00"),
                        EstadoCuenta.ACTIVA, clienteActivo())
        );
    }

    @Test
    void cuentaForInactiveClienteShouldFail() {
        assertThrows(ClienteInactivoException.class, () ->
                Cuenta.create("ACC-004", TipoCuenta.AHORRO, BigDecimal.ZERO,
                        EstadoCuenta.ACTIVA, clienteInactivo())
        );
    }

    @Test
    void cuentaWithValidDataShouldSetSaldoDisponibleEqualToSaldoInicial() {
        BigDecimal saldo = new BigDecimal("200.00");
        Cuenta cuenta = Cuenta.create("ACC-005", TipoCuenta.AHORRO, saldo,
                EstadoCuenta.ACTIVA, clienteActivo());
        assertEquals(saldo, cuenta.getSaldoDisponible());
    }
}
