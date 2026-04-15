package com.banking.accounts.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_CLIENTE = "cliente.events";
    public static final String EXCHANGE_DLX = "cliente.events.dlx";
    public static final String QUEUE_CLIENTE_CREATED = "accounts.cliente.created";
    public static final String QUEUE_CLIENTE_DESACTIVADO = "accounts.cliente.desactivado";
    public static final String QUEUE_DLQ = "accounts.cliente.dlq";
    public static final String ROUTING_CREATED = "cliente.created";
    public static final String ROUTING_DESACTIVADO = "cliente.desactivado";

    @Bean
    public TopicExchange clienteExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_CLIENTE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    @Bean
    public Queue clienteCreatedQueue() {
        return QueueBuilder.durable(QUEUE_CLIENTE_CREATED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dead")
                .build();
    }

    @Bean
    public Queue clienteDesactivadoQueue() {
        return QueueBuilder.durable(QUEUE_CLIENTE_DESACTIVADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dead")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding clienteCreatedBinding(Queue clienteCreatedQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(clienteCreatedQueue).to(clienteExchange).with(ROUTING_CREATED);
    }

    @Bean
    public Binding clienteDesactivadoBinding(Queue clienteDesactivadoQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(clienteDesactivadoQueue).to(clienteExchange).with(ROUTING_DESACTIVADO);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("dead");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        return factory;
    }
}
