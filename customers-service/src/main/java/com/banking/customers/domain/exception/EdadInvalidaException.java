package com.banking.customers.domain.exception;

public class EdadInvalidaException extends BusinessRuleException {

    public EdadInvalidaException() {
        super("La edad debe ser mayor o igual a 18 años");
    }
}
