package com.banking.customers.unit.application;

import com.banking.customers.application.dto.ActualizarClienteRequest;
import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.dto.CrearClienteRequest;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoStructureTest {

    @Test
    void crearClienteRequestShouldHaveAllRequiredFields() {
        CrearClienteRequest request = new CrearClienteRequest(
                "Jose Lema", Genero.MASCULINO, 30,
                "1713175071", "Otavalo", "0987654321",
                "Admin1234", EstadoCliente.ACTIVO);
        assertEquals("Jose Lema", request.getNombre());
        assertEquals(Genero.MASCULINO, request.getGenero());
        assertEquals(30, request.getEdad());
        assertEquals("1713175071", request.getIdentificacion());
        assertEquals("Otavalo", request.getDireccion());
        assertEquals("0987654321", request.getTelefono());
        assertEquals("Admin1234", request.getContrasena());
        assertEquals(EstadoCliente.ACTIVO, request.getEstado());
    }

    @Test
    void actualizarClienteRequestShouldAllowNullFields() {
        assertDoesNotThrow(() -> new ActualizarClienteRequest(
                null, null, null, null, null, null, null, null));
    }

    @Test
    void clienteResponseShouldNotExposeContrasena() {
        assertThrows(NoSuchMethodException.class,
                () -> ClienteResponse.class.getMethod("getContrasena"));
    }

    @Test
    void clienteResponseShouldMapAllPersonaFields() {
        Cliente cliente = Cliente.reconstitute(1L, "Jose Lema", Genero.MASCULINO, 30,
                "1713175071", "Otavalo", "0987654321", "Admin1234", EstadoCliente.ACTIVO);
        ClienteResponse response = ClienteResponse.from(cliente);
        assertEquals(1L, response.getClienteId());
        assertEquals("Jose Lema", response.getNombre());
        assertEquals("MASCULINO", response.getGenero());
        assertEquals(30, response.getEdad());
        assertEquals("1713175071", response.getIdentificacion());
        assertEquals("Otavalo", response.getDireccion());
        assertEquals("0987654321", response.getTelefono());
        assertEquals("ACTIVO", response.getEstado());
    }
}
