package com.banking.accounts.domain.exception;

public class SaldoInsuficienteException extends BusinessRuleException {

    public SaldoInsuficienteException() {
        super("Saldo no disponible");
    }
}
