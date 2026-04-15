package com.banking.accounts.domain.exception;

public class SaldoMinimoInsuficienteException extends BusinessRuleException {

    public SaldoMinimoInsuficienteException() {
        super("La cuenta corriente requiere un saldo inicial mínimo de $50");
    }
}
