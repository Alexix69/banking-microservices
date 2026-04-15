package com.banking.customers.unit.usecase;

import com.banking.customers.application.dto.ActualizarClienteRequest;
import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.usecase.ActualizarClienteUseCase;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.exception.IdentificacionDuplicadaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import com.banking.customers.domain.port.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActualizarClienteUseCaseTest {

    private ClienteRepository clienteRepository;
    private ActualizarClienteUseCase useCase;

    private static final Long ID = 1L;
    private static final String VALID_IDENTIFICACION = "1713175071";

    private Cliente clienteExistente() {
        return Cliente.reconstitute(ID, "Jose Lema", Genero.MASCULINO, 30,
                VALID_IDENTIFICACION, "Otavalo sn y Quito", "098254785",
                "Password1", EstadoCliente.ACTIVO);
    }

    @BeforeEach
    void setUp() {
        clienteRepository = mock(ClienteRepository.class);
        useCase = new ActualizarClienteUseCase(clienteRepository);
    }

    @Test
    void existingClienteWithValidDataShouldUpdateAndReturnResponse() {
        Cliente existente = clienteExistente();
        ActualizarClienteRequest request = new ActualizarClienteRequest(
                "Jose Actualizado", Genero.MASCULINO, 35,
                VALID_IDENTIFICACION, "Nueva Direccion", "099999999",
                null, null);
        Cliente actualizado = Cliente.reconstitute(ID, "Jose Actualizado", Genero.MASCULINO, 35,
                VALID_IDENTIFICACION, "Nueva Direccion", "099999999",
                "Password1", EstadoCliente.ACTIVO);
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByIdentificacionAndIdNot(VALID_IDENTIFICACION, ID)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(actualizado);

        ClienteResponse response = useCase.ejecutar(ID, request);

        verify(clienteRepository).save(any(Cliente.class));
        assertThat(response.getNombre()).isEqualTo("Jose Actualizado");
        assertThat(response.getEdad()).isEqualTo(35);
    }

    @Test
    void nonExistingClienteShouldThrowClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        ActualizarClienteRequest request = new ActualizarClienteRequest(
                "Nombre", null, null, null, null, null, null, null);

        assertThatThrownBy(() -> useCase.ejecutar(99L, request))
                .isInstanceOf(ClienteNotFoundException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void updateWithDuplicateIdentificacionShouldThrowException() {
        String otraIdentificacion = "0650789428";
        Cliente existente = clienteExistente();
        ActualizarClienteRequest request = new ActualizarClienteRequest(
                null, null, null, otraIdentificacion, null, null, null, null);
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByIdentificacionAndIdNot(otraIdentificacion, ID)).thenReturn(true);

        assertThatThrownBy(() -> useCase.ejecutar(ID, request))
                .isInstanceOf(IdentificacionDuplicadaException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void updateShouldOnlyModifyProvidedFields() {
        Cliente existente = clienteExistente();
        ActualizarClienteRequest request = new ActualizarClienteRequest(
                "Nuevo Nombre", null, null, null, "Nueva Direccion", null, null, null);
        Cliente actualizado = Cliente.reconstitute(ID, "Nuevo Nombre", Genero.MASCULINO, 30,
                VALID_IDENTIFICACION, "Nueva Direccion", "098254785",
                "Password1", EstadoCliente.ACTIVO);
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByIdentificacionAndIdNot(any(), any())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(actualizado);

        ClienteResponse response = useCase.ejecutar(ID, request);

        assertThat(response.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(response.getDireccion()).isEqualTo("Nueva Direccion");
        assertThat(response.getGenero()).isEqualTo("MASCULINO");
        assertThat(response.getEdad()).isEqualTo(30);
        assertThat(response.getIdentificacion()).isEqualTo(VALID_IDENTIFICACION);
        assertThat(response.getTelefono()).isEqualTo("098254785");
    }

    @Test
    void updateShouldRevalidateEdadAndIdentificacion() {
        Cliente existente = clienteExistente();
        ActualizarClienteRequest request = new ActualizarClienteRequest(
                null, null, 15, null, null, null, null, null);
        when(clienteRepository.findById(ID)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> useCase.ejecutar(ID, request))
                .isInstanceOf(com.banking.customers.domain.exception.EdadInvalidaException.class);

        verify(clienteRepository, never()).save(any());
    }
}
