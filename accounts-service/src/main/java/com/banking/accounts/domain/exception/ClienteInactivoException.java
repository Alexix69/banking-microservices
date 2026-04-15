package com.banking.accounts.domain.exception;

public class ClienteInactivoException extends BusinessRuleException {

    public ClienteInactivoException() {
        super("El cliente está inactivo");
    }
}
