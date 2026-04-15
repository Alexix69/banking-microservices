package com.banking.customers.application.usecase;

import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.dto.CrearClienteRequest;
import com.banking.customers.domain.event.ClienteCreatedEvent;
import com.banking.customers.domain.exception.IdentificacionDuplicadaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.domain.port.EventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CrearClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final EventPublisher eventPublisher;

    public CrearClienteUseCase(ClienteRepository clienteRepository, EventPublisher eventPublisher) {
        this.clienteRepository = clienteRepository;
        this.eventPublisher = eventPublisher;
    }

    public ClienteResponse ejecutar(CrearClienteRequest request) {
        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new IdentificacionDuplicadaException(request.getIdentificacion());
        }
        Cliente cliente = Cliente.create(request.getNombre(), request.getGenero(), request.getEdad(),
                request.getIdentificacion(), request.getDireccion(), request.getTelefono(),
                request.getContrasena(), request.getEstado());
        Cliente clientePersistido = clienteRepository.save(cliente);
        eventPublisher.publish(new ClienteCreatedEvent(
                clientePersistido.getId(), clientePersistido.getNombre(), EstadoCliente.ACTIVO));
        return ClienteResponse.from(clientePersistido);
    }
}
