package com.banking.customers.unit.usecase;

import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.usecase.ConsultarClienteUseCase;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import com.banking.customers.domain.port.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ConsultarClienteUseCaseTest {

    private ClienteRepository clienteRepository;
    private ConsultarClienteUseCase useCase;

    @BeforeEach
    void setUp() {
        clienteRepository = mock(ClienteRepository.class);
        useCase = new ConsultarClienteUseCase(clienteRepository);
    }

    @Test
    void existingClienteShouldReturnClienteResponse() {
        Cliente cliente = Cliente.reconstitute(1L, "Jose Lema", Genero.MASCULINO,
                30, "1713175071", "Otavalo sn y Quito", "098254785",
                "Password1", EstadoCliente.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = useCase.ejecutar(1L);

        assertThat(response).isNotNull();
        assertThat(response.getClienteId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Jose Lema");
        assertThat(response.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void nonExistingClienteShouldThrowClienteNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(99L))
                .isInstanceOf(ClienteNotFoundException.class);
    }
}
