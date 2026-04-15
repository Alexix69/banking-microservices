package com.banking.accounts.domain.exception;

public class MovimientoNotFoundException extends ResourceNotFoundException {

    public MovimientoNotFoundException(Long id) {
        super("Movimiento no encontrado con ID: " + id);
    }
}
