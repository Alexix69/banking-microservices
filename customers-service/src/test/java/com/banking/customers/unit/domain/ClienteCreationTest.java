package com.banking.customers.unit.domain;

import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.exception.ContrasenaInvalidaException;
import com.banking.customers.domain.exception.EdadInvalidaException;
import com.banking.customers.domain.exception.IdentificacionInvalidaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteCreationTest {

    private static final String IDENTIFICACION_VALIDA = "1713175071";
    private static final String CONTRASENA_VALIDA = "Contrasena1";
    private static final String NOMBRE = "Jose Lema";
    private static final String DIRECCION = "Otavalo sn y principal";
    private static final String TELEFONO = "+593991234567";

    @Test
    void clienteWithValidDataShouldCreateInstance() {
        Cliente cliente = Cliente.create(
                NOMBRE, Genero.MASCULINO, 30,
                IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                CONTRASENA_VALIDA, EstadoCliente.ACTIVO
        );

        assertNotNull(cliente);
        assertEquals(NOMBRE, cliente.getNombre());
        assertEquals(Genero.MASCULINO, cliente.getGenero());
        assertEquals(30, cliente.getEdad());
        assertEquals(IDENTIFICACION_VALIDA, cliente.getIdentificacion());
        assertEquals(EstadoCliente.ACTIVO, cliente.getEstado());
    }

    @Test
    void clienteWithAgeLessThan18ShouldThrowEdadInvalidaException() {
        assertThrows(EdadInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.MASCULINO, 17,
                        IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }

    @Test
    void clienteWithInvalidIdentificacionShouldThrowException() {
        assertThrows(IdentificacionInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.MASCULINO, 25,
                        "1234567890", DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }

    @Test
    void clienteWithInvalidContrasenaShouldThrowException() {
        assertThrows(ContrasenaInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.MASCULINO, 25,
                        IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                        "sinmayus1", EstadoCliente.ACTIVO)
        );
    }

    @Test
    void clienteCreationShouldNotRegisterEventDirectly() {
        Cliente cliente = Cliente.create(
                NOMBRE, Genero.MASCULINO, 30,
                IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                CONTRASENA_VALIDA, EstadoCliente.ACTIVO
        );

        List<DomainEvent> eventos = cliente.consumirEventos();

        assertTrue(eventos.isEmpty());
    }
}
