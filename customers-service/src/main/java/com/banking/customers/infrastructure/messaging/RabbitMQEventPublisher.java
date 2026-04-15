package com.banking.customers.infrastructure.messaging;

import com.banking.customers.domain.event.ClienteCreatedEvent;
import com.banking.customers.domain.event.ClienteDesactivadoEvent;
import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.port.EventPublisher;
import com.banking.customers.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private static final String ROUTING_CREATED = "cliente.created";
    private static final String ROUTING_DESACTIVADO = "cliente.desactivado";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event instanceof ClienteCreatedEvent) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_CLIENTE, ROUTING_CREATED, event);
        } else if (event instanceof ClienteDesactivadoEvent) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_CLIENTE, ROUTING_DESACTIVADO, event);
        }
    }
}
