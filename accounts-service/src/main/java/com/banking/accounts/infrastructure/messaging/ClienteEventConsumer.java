package com.banking.accounts.infrastructure.messaging;

import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.infrastructure.config.RabbitMQConfig;
import com.banking.accounts.infrastructure.mapper.ClienteMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ClienteEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClienteEventConsumer.class);

    private final ClienteProyeccionRepository clienteProyeccionRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteMapper clienteMapper;

    public ClienteEventConsumer(ClienteProyeccionRepository clienteProyeccionRepository,
                                CuentaRepository cuentaRepository,
                                ClienteMapper clienteMapper) {
        this.clienteProyeccionRepository = clienteProyeccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.clienteMapper = clienteMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CLIENTE_CREATED)
    public void consumirClienteCreated(ClienteCreatedMessage message,
                                       Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            ClienteProyeccion proyeccion = clienteMapper.toClienteProyeccion(message);
            clienteProyeccionRepository.save(proyeccion);
            channel.basicAck(tag, false);
            log.info("ClienteCreatedEvent procesado: clienteId={}", message.getClienteId());
        } catch (Exception e) {
            log.error("Error procesando ClienteCreatedEvent: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CLIENTE_DESACTIVADO)
    public void consumirClienteDesactivado(ClienteDesactivadoMessage message,
                                           Channel channel,
                                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            clienteProyeccionRepository.desactivar(message.getClienteId());
            cuentaRepository.desactivarTodasPorClienteId(message.getClienteId());
            channel.basicAck(tag, false);
            log.info("ClienteDesactivadoEvent procesado: clienteId={}", message.getClienteId());
        } catch (Exception e) {
            log.error("Error procesando ClienteDesactivadoEvent: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
