package com.banking.accounts.domain.exception;

public class NumeroCuentaDuplicadoException extends DuplicateResourceException {

    public NumeroCuentaDuplicadoException(String numeroCuenta) {
        super("Ya existe una cuenta con el número " + numeroCuenta);
    }
}
