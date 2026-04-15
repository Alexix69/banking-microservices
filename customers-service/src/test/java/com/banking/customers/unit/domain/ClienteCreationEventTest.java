package com.banking.customers.unit.domain;

import com.banking.customers.domain.event.ClienteDesactivadoEvent;
import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteCreationEventTest {

    @Test
    void clienteCreationShouldLeaveEmptyDomainEvents() {
        Cliente cliente = Cliente.create("Jose Lema", Genero.MASCULINO, 18,
                "1713175071", "Otavalo", "0987654321", "Admin1234", EstadoCliente.ACTIVO);
        assertTrue(cliente.consumirEventos().isEmpty());
    }

    @Test
    void desactivarShouldRegisterClienteDesactivadoEvent() {
        Cliente cliente = Cliente.reconstitute(10L, "Jose Lema", Genero.MASCULINO, 18,
                "1713175071", "Otavalo", "0987654321", "Admin1234", EstadoCliente.ACTIVO);
        cliente.desactivar();
        List<DomainEvent> eventos = cliente.consumirEventos();
        assertEquals(1, eventos.size());
        assertInstanceOf(ClienteDesactivadoEvent.class, eventos.get(0));
        assertEquals(10L, ((ClienteDesactivadoEvent) eventos.get(0)).getClienteId());
    }

    @Test
    void consumirEventosShouldReturnCopyAndClearList() {
        Cliente cliente = Cliente.reconstitute(5L, "Ana Torres", Genero.FEMENINO, 25,
                "1713175071", "Quito", "0912345678", "Secure12", EstadoCliente.ACTIVO);
        cliente.desactivar();
        List<DomainEvent> primera = cliente.consumirEventos();
        assertEquals(1, primera.size());
        assertEquals(EstadoCliente.INACTIVO, cliente.getEstado());
    }

    @Test
    void consumirEventosCalledTwiceShouldReturnEmptySecondTime() {
        Cliente cliente = Cliente.reconstitute(7L, "Luis Gomez", Genero.MASCULINO, 30,
                "1713175071", "Guayaquil", "0923456789", "Pass1234", EstadoCliente.ACTIVO);
        cliente.desactivar();
        cliente.consumirEventos();
        List<DomainEvent> segunda = cliente.consumirEventos();
        assertTrue(segunda.isEmpty());
    }
}
