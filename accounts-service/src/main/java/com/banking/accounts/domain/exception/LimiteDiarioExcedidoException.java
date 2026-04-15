package com.banking.accounts.domain.exception;

public class LimiteDiarioExcedidoException extends BusinessRuleException {

    public LimiteDiarioExcedidoException() {
        super("Límite de retiro diario excedido");
    }
}
