package com.banking.customers.domain.event;

public final class ClienteDesactivadoEvent implements DomainEvent {

    private final Long clienteId;

    public ClienteDesactivadoEvent(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getClienteId() {
        return clienteId;
    }
}
