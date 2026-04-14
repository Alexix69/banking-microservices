package com.banking.customers.domain.exception;

public class ContrasenaInvalidaException extends BusinessRuleException {

    public ContrasenaInvalidaException() {
        super("La contraseña debe tener al menos 8 caracteres alfanuméricos y una mayúscula");
    }
}
