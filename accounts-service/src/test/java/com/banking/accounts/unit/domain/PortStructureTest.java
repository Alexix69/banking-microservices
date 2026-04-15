package com.banking.accounts.unit.domain;

import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PortStructureTest {

    @Test
    void cuentaRepositoryPortShouldBeAnInterface() {
        assertTrue(CuentaRepository.class.isInterface());
    }

    @Test
    void movimientoRepositoryPortShouldBeAnInterface() {
        assertTrue(MovimientoRepository.class.isInterface());
    }

    @Test
    void clienteProyeccionRepositoryPortShouldBeAnInterface() {
        assertTrue(ClienteProyeccionRepository.class.isInterface());
    }
}
