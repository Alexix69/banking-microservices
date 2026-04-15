package com.banking.customers.application.usecase;

import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.domain.port.EventPublisher;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EliminarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final EventPublisher eventPublisher;

    public EliminarClienteUseCase(ClienteRepository clienteRepository, EventPublisher eventPublisher) {
        this.clienteRepository = clienteRepository;
        this.eventPublisher = eventPublisher;
    }

    public ClienteResponse ejecutar(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
        cliente.desactivar();
        Cliente clienteDesactivado = clienteRepository.save(cliente);
        List<DomainEvent> eventos = cliente.consumirEventos();
        eventos.forEach(eventPublisher::publish);
        return ClienteResponse.from(clienteDesactivado);
    }
}
