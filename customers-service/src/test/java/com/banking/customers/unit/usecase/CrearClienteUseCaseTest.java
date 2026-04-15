package com.banking.customers.unit.usecase;

import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.dto.CrearClienteRequest;
import com.banking.customers.application.usecase.CrearClienteUseCase;
import com.banking.customers.domain.event.ClienteCreatedEvent;
import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.exception.IdentificacionDuplicadaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.domain.port.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CrearClienteUseCaseTest {

    private ClienteRepository clienteRepository;
    private EventPublisher eventPublisher;
    private CrearClienteUseCase useCase;

    private static final String VALID_IDENTIFICACION = "1713175071";
    private static final String VALID_CONTRASENA = "Password1";

    private CrearClienteRequest buildRequest() {
        return new CrearClienteRequest(
                "Jose Lema",
                Genero.MASCULINO,
                30,
                VALID_IDENTIFICACION,
                "Otavalo sn y Quito",
                "098254785",
                VALID_CONTRASENA,
                EstadoCliente.ACTIVO
        );
    }

    @BeforeEach
    void setUp() {
        clienteRepository = mock(ClienteRepository.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new CrearClienteUseCase(clienteRepository, eventPublisher);
    }

    @Test
    void clienteWithValidDataShouldPersistAndReturnResponse() {
        CrearClienteRequest request = buildRequest();
        Cliente saved = Cliente.reconstitute(1L, request.getNombre(), request.getGenero(),
                request.getEdad(), request.getIdentificacion(), request.getDireccion(),
                request.getTelefono(), request.getContrasena(), request.getEstado());
        when(clienteRepository.existsByIdentificacion(VALID_IDENTIFICACION)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(saved);

        ClienteResponse response = useCase.ejecutar(request);

        verify(clienteRepository).save(any(Cliente.class));
        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Jose Lema");
        assertThat(response.getClienteId()).isEqualTo(1L);
    }

    @Test
    void clienteWithDuplicateIdentificacionShouldThrowException() {
        when(clienteRepository.existsByIdentificacion(VALID_IDENTIFICACION)).thenReturn(true);

        assertThatThrownBy(() -> useCase.ejecutar(buildRequest()))
                .isInstanceOf(IdentificacionDuplicadaException.class);

        verify(clienteRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void clienteWithValidDataShouldPublishClienteCreatedEvent() {
        CrearClienteRequest request = buildRequest();
        Cliente saved = Cliente.reconstitute(1L, request.getNombre(), request.getGenero(),
                request.getEdad(), request.getIdentificacion(), request.getDireccion(),
                request.getTelefono(), request.getContrasena(), request.getEstado());
        when(clienteRepository.existsByIdentificacion(VALID_IDENTIFICACION)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(saved);

        useCase.ejecutar(request);

        verify(eventPublisher).publish(any(ClienteCreatedEvent.class));
    }

    @Test
    void clienteCreatedEventShouldContainRealIdAfterPersistence() {
        CrearClienteRequest request = buildRequest();
        Cliente saved = Cliente.reconstitute(42L, request.getNombre(), request.getGenero(),
                request.getEdad(), request.getIdentificacion(), request.getDireccion(),
                request.getTelefono(), request.getContrasena(), request.getEstado());
        when(clienteRepository.existsByIdentificacion(VALID_IDENTIFICACION)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(saved);

        useCase.ejecutar(request);

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        DomainEvent captured = captor.getValue();
        assertThat(captured).isInstanceOf(ClienteCreatedEvent.class);
        assertThat(((ClienteCreatedEvent) captured).getClienteId()).isEqualTo(42L);
    }

    @Test
    void clienteResponseShouldNotContainContrasena() {
        CrearClienteRequest request = buildRequest();
        Cliente saved = Cliente.reconstitute(1L, request.getNombre(), request.getGenero(),
                request.getEdad(), request.getIdentificacion(), request.getDireccion(),
                request.getTelefono(), request.getContrasena(), request.getEstado());
        when(clienteRepository.existsByIdentificacion(VALID_IDENTIFICACION)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(saved);

        ClienteResponse response = useCase.ejecutar(request);

        assertThat(response).isNotNull();
        assertThat(response).hasNoNullFieldsOrPropertiesExcept();
        java.lang.reflect.Field[] fields = response.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            assertThat(field.getName()).doesNotContainIgnoringCase("contrasena");
            assertThat(field.getName()).doesNotContainIgnoringCase("password");
        }
    }
}
