package com.banking.customers.unit.domain;

import com.banking.customers.domain.exception.ContrasenaInvalidaException;
import com.banking.customers.domain.exception.IdentificacionInvalidaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteValidationTest {

    private static final String CONTRASENA_VALIDA = "Contrasena1";
    private static final String NOMBRE = "Test Usuario";
    private static final String DIRECCION = "Calle 1";
    private static final String TELEFONO = "+593991234567";

    @Test
    void identificacionNullShouldThrowException() {
        assertThrows(IdentificacionInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.FEMENINO, 25,
                        null, DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }

    @Test
    void identificacionWithThirdDigitGreaterThan5ShouldThrowException() {
        assertThrows(IdentificacionInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.FEMENINO, 25,
                        "1767180736", DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }

    @Test
    void identificacionWithWrongVerifierDigitShouldThrowException() {
        assertThrows(IdentificacionInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.FEMENINO, 25,
                        "0102030405", DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }

    @Test
    void identificacionWithInvalidProvinciaShouldThrowException() {
        assertThrows(IdentificacionInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.FEMENINO, 25,
                        "9999999999", DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }

    @Test
    void contrasenaWithLessThanEightCharsShouldThrowException() {
        assertThrows(ContrasenaInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.MASCULINO, 25,
                        "1713175071", DIRECCION, TELEFONO,
                        "Abc123", EstadoCliente.ACTIVO)
        );
    }

    @Test
    void contrasenaWithoutUppercaseShouldThrowException() {
        assertThrows(ContrasenaInvalidaException.class, () ->
                Cliente.create(NOMBRE, Genero.MASCULINO, 25,
                        "1713175071", DIRECCION, TELEFONO,
                        "sinmayus1", EstadoCliente.ACTIVO)
        );
    }

    @Test
    void validGeneroValuesShouldBeAccepted() {
        assertDoesNotThrow(() ->
                Cliente.create(NOMBRE, Genero.MASCULINO, 20,
                        "1713175071", DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
        assertDoesNotThrow(() ->
                Cliente.create(NOMBRE, Genero.FEMENINO, 20,
                        "1700000001", DIRECCION, TELEFONO,
                        CONTRASENA_VALIDA, EstadoCliente.ACTIVO)
        );
    }
}
