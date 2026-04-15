package com.banking.customers.domain.port;

import com.banking.customers.domain.event.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);
}
