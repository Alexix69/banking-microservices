package com.banking.accounts.domain.exception;

public class CuentaNotFoundException extends ResourceNotFoundException {

    public CuentaNotFoundException(Long id) {
        super("Cuenta no encontrada con ID: " + id);
    }
}
