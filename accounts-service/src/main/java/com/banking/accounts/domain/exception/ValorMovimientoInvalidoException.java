package com.banking.accounts.domain.exception;

public class ValorMovimientoInvalidoException extends BusinessRuleException {

    public ValorMovimientoInvalidoException() {
        super("El valor del movimiento no puede ser cero");
    }
}
