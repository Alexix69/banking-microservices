package com.banking.customers.domain.exception;

public class IdentificacionInvalidaException extends BusinessRuleException {

    public IdentificacionInvalidaException(String identificacion) {
        super("La identificación '" + identificacion + "' no es válida");
    }
}
