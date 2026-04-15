package com.banking.customers.unit.domain;

import com.banking.customers.domain.event.ClienteCreatedEvent;
import com.banking.customers.domain.event.ClienteDesactivadoEvent;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.domain.port.EventPublisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteEventTest {

    @Test
    void clienteCreatedEventShouldHaveCorrectFields() {
        ClienteCreatedEvent event = new ClienteCreatedEvent(1L, "Jose Lema", EstadoCliente.ACTIVO);
        assertEquals(1L, event.getClienteId());
        assertEquals("Jose Lema", event.getNombre());
        assertEquals(EstadoCliente.ACTIVO, event.getEstado());
    }

    @Test
    void clienteDesactivadoEventShouldHaveClienteId() {
        ClienteDesactivadoEvent event = new ClienteDesactivadoEvent(42L);
        assertEquals(42L, event.getClienteId());
    }

    @Test
    void clienteRepositoryPortShouldBeAnInterface() {
        assertTrue(ClienteRepository.class.isInterface());
    }

    @Test
    void eventPublisherPortShouldBeAnInterface() {
        assertTrue(EventPublisher.class.isInterface());
    }
}
