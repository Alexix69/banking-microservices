package com.banking.customers.domain.exception;

public class ContrasenaInvalidaException extends BusinessRuleException {

    public ContrasenaInvalidaException() {
        super("La contraseña no cumple la política de seguridad");
    }
}
