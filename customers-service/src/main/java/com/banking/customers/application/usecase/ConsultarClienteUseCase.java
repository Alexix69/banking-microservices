package com.banking.customers.application.usecase;

import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.port.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsultarClienteUseCase {

    private final ClienteRepository clienteRepository;

    public ConsultarClienteUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponse ejecutar(Long id) {
        return clienteRepository.findById(id)
                .map(ClienteResponse::from)
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }
}
