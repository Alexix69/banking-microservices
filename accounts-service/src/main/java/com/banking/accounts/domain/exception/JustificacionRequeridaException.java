package com.banking.accounts.domain.exception;

public class JustificacionRequeridaException extends BusinessRuleException {

    public JustificacionRequeridaException() {
        super("El ajuste requiere una justificación");
    }
}
