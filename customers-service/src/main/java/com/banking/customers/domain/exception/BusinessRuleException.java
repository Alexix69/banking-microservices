package com.banking.customers.domain.exception;

public abstract class BusinessRuleException extends DomainException {

    protected BusinessRuleException(String message) {
        super(message);
    }
}
