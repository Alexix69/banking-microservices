package com.banking.accounts.domain.exception;

public class SaldoInicialInvalidoException extends BusinessRuleException {

    public SaldoInicialInvalidoException() {
        super("El saldo inicial debe ser mayor o igual a cero");
    }
}
