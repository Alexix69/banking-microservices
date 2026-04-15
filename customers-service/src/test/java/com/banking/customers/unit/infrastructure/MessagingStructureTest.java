package com.banking.customers.unit.infrastructure;

import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.port.EventPublisher;
import com.banking.customers.infrastructure.mapper.CustomerMapper;
import com.banking.customers.infrastructure.messaging.RabbitMQEventPublisher;
import com.banking.customers.infrastructure.persistence.ClienteJpaEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagingStructureTest {

    @Test
    void rabbitMQEventPublisherShouldImplementEventPublisher() {
        assertTrue(EventPublisher.class.isAssignableFrom(RabbitMQEventPublisher.class));
    }

    @Test
    void clienteMapperShouldHaveToClienteAndToJpaEntityMethods() {
        Method[] methods = CustomerMapper.class.getDeclaredMethods();
        boolean hasToCliente = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("toCliente") && m.getReturnType().equals(Cliente.class));
        boolean hasToJpaEntity = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("toJpaEntity") && m.getReturnType().equals(ClienteJpaEntity.class));
        assertTrue(hasToCliente);
        assertTrue(hasToJpaEntity);
    }
}
