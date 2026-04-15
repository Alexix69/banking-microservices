package com.banking.accounts.domain.exception;

public abstract class DuplicateResourceException extends DomainException {

    protected DuplicateResourceException(String message) {
        super(message);
    }
}
