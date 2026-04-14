package com.banking.customers.domain.event;

import com.banking.customers.domain.model.EstadoCliente;

public class ClienteCreatedEvent implements DomainEvent {

    private final Long clienteId;
    private final String nombre;
    private final EstadoCliente estado;

    public ClienteCreatedEvent(Long clienteId, String nombre, EstadoCliente estado) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.estado = estado;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoCliente getEstado() {
        return estado;
    }
}
