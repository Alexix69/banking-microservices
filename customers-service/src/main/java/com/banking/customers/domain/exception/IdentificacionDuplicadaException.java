package com.banking.customers.domain.exception;

public class IdentificacionDuplicadaException extends DuplicateResourceException {

    public IdentificacionDuplicadaException(String identificacion) {
        super("Ya existe un cliente con la identificación " + identificacion);
    }
}
