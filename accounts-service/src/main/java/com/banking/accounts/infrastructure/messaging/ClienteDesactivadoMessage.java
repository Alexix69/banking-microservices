package com.banking.accounts.infrastructure.messaging;

public class ClienteDesactivadoMessage {

    private Long clienteId;

    public ClienteDesactivadoMessage() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
