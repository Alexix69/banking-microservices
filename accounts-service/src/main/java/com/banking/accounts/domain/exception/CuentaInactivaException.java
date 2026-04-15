package com.banking.accounts.domain.exception;

public class CuentaInactivaException extends BusinessRuleException {

    public CuentaInactivaException() {
        super("La cuenta está inactiva y no puede recibir movimientos");
    }
}
