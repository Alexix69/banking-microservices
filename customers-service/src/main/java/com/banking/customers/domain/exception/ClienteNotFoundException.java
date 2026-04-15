package com.banking.customers.domain.exception;

public class ClienteNotFoundException extends ResourceNotFoundException {

    public ClienteNotFoundException(Long id) {
        super("Cliente no encontrado con ID: " + id);
    }
}
