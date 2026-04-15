package com.banking.accounts.domain.exception;

public abstract class ResourceNotFoundException extends DomainException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
