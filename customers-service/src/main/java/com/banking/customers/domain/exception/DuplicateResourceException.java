package com.banking.customers.domain.exception;

public abstract class DuplicateResourceException extends DomainException {

    protected DuplicateResourceException(String message) {
        super(message);
    }
}
