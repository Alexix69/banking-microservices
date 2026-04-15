package com.banking.accounts.domain.exception;

public class ClienteNotFoundException extends ResourceNotFoundException {

    public ClienteNotFoundException(Long clienteId) {
        super("Cliente no encontrado con ID: " + clienteId);
    }
}
