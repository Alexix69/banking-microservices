package com.banking.customers.unit.domain;

import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteDeactivationTest {

    private static final String IDENTIFICACION_VALIDA = "1713175071";
    private static final String CONTRASENA_VALIDA = "Contrasena1";
    private static final String NOMBRE = "Jose Lema";
    private static final String DIRECCION = "Otavalo sn y principal";
    private static final String TELEFONO = "+593991234567";

    @Test
    void desactivatedClienteShouldHaveInactiveState() {
        Cliente cliente = Cliente.create(
                NOMBRE, Genero.MASCULINO, 30,
                IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                CONTRASENA_VALIDA, EstadoCliente.ACTIVO
        );

        cliente.desactivar();

        assertEquals(EstadoCliente.INACTIVO, cliente.getEstado());
    }

    @Test
    void desactivatedClienteShouldHaveInactiveState_whenPreviouslyActive() {
        Cliente cliente = Cliente.reconstitute(
                1L, NOMBRE, Genero.MASCULINO, 30,
                IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                CONTRASENA_VALIDA, EstadoCliente.ACTIVO
        );

        cliente.desactivar();

        assertEquals(EstadoCliente.INACTIVO, cliente.getEstado());
    }
}
