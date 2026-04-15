package com.banking.customers.unit.usecase;

import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.usecase.EliminarClienteUseCase;
import com.banking.customers.domain.event.ClienteDesactivadoEvent;
import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.domain.port.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EliminarClienteUseCaseTest {

    private ClienteRepository clienteRepository;
    private EventPublisher eventPublisher;
    private EliminarClienteUseCase useCase;

    private static final Long ID = 1L;

    private Cliente clienteActivo() {
        return Cliente.reconstitute(ID, "Jose Lema", Genero.MASCULINO, 30,
                "1713175071", "Otavalo sn y Quito", "098254785",
                "Password1", EstadoCliente.ACTIVO);
    }

    @BeforeEach
    void setUp() {
        clienteRepository = mock(ClienteRepository.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new EliminarClienteUseCase(clienteRepository, eventPublisher);
    }

    @Test
    void existingClienteShouldDeactivateAndPublishEvent() {
        Cliente cliente = clienteActivo();
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = useCase.ejecutar(ID);

        verify(clienteRepository).save(any(Cliente.class));
        verify(eventPublisher).publish(any(ClienteDesactivadoEvent.class));
        assertThat(response.getEstado()).isEqualTo("INACTIVO");
    }

    @Test
    void nonExistingClienteShouldThrowClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(99L))
                .isInstanceOf(ClienteNotFoundException.class);

        verify(clienteRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void deactivatedClienteShouldPublishClienteDesactivadoEvent() {
        Cliente cliente = clienteActivo();
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.ejecutar(ID);

        verify(eventPublisher).publish(any(ClienteDesactivadoEvent.class));
    }

    @Test
    void deactivationShouldNeverCheckForActiveCuentas() {
        Cliente cliente = clienteActivo();
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.ejecutar(ID);

        verify(clienteRepository).findById(ID);
        verify(clienteRepository).save(any(Cliente.class));
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void publishedEventShouldContainCorrectClienteId() {
        Cliente cliente = clienteActivo();
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.ejecutar(ID);

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        DomainEvent captured = captor.getValue();
        assertThat(captured).isInstanceOf(ClienteDesactivadoEvent.class);
        assertThat(((ClienteDesactivadoEvent) captured).getClienteId()).isEqualTo(ID);
    }
}
