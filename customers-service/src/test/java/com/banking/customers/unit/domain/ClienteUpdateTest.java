package com.banking.customers.unit.domain;

import com.banking.customers.domain.exception.EdadInvalidaException;
import com.banking.customers.domain.exception.IdentificacionInvalidaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteUpdateTest {

    private static final String IDENTIFICACION_VALIDA = "1713175071";
    private static final String CONTRASENA_VALIDA = "Contrasena1";
    private static final String NOMBRE = "Jose Lema";
    private static final String DIRECCION = "Otavalo sn y principal";
    private static final String TELEFONO = "+593991234567";

    private Cliente clienteBase() {
        return Cliente.create(
                NOMBRE, Genero.MASCULINO, 30,
                IDENTIFICACION_VALIDA, DIRECCION, TELEFONO,
                CONTRASENA_VALIDA, EstadoCliente.ACTIVO
        );
    }

    @Test
    void updateWithValidDataShouldModifyFields() {
        Cliente cliente = clienteBase();

        cliente.actualizarDatos(
                "Mariana Montalvo", Genero.FEMENINO, 25,
                IDENTIFICACION_VALIDA, "Amazonas y NNUU", "+593997654321"
        );

        assertEquals("Mariana Montalvo", cliente.getNombre());
        assertEquals(Genero.FEMENINO, cliente.getGenero());
        assertEquals(25, cliente.getEdad());
        assertEquals("Amazonas y NNUU", cliente.getDireccion());
        assertEquals("+593997654321", cliente.getTelefono());
    }

    @Test
    void updateWithInvalidAgeShouldThrowException() {
        Cliente cliente = clienteBase();

        assertThrows(EdadInvalidaException.class, () ->
                cliente.actualizarDatos(
                        NOMBRE, Genero.MASCULINO, 17,
                        IDENTIFICACION_VALIDA, DIRECCION, TELEFONO
                )
        );
    }

    @Test
    void updateWithInvalidIdentificacionShouldThrowException() {
        Cliente cliente = clienteBase();

        assertThrows(IdentificacionInvalidaException.class, () ->
                cliente.actualizarDatos(
                        NOMBRE, Genero.MASCULINO, 30,
                        "1234567890", DIRECCION, TELEFONO
                )
        );
    }
}
