package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.CrearCuentaRequest;
import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.application.usecase.CrearCuentaUseCase;
import com.banking.accounts.domain.exception.ClienteInactivoException;
import com.banking.accounts.domain.exception.NumeroCuentaDuplicadoException;
import com.banking.accounts.domain.exception.SaldoMinimoInsuficienteException;
import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.EstadoCliente;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CrearCuentaUseCaseTest {

    private CuentaRepository cuentaRepository;
    private ClienteProyeccionRepository clienteProyeccionRepository;
    private CrearCuentaUseCase useCase;

    @BeforeEach
    void setUp() {
        cuentaRepository = mock(CuentaRepository.class);
        clienteProyeccionRepository = mock(ClienteProyeccionRepository.class);
        useCase = new CrearCuentaUseCase(cuentaRepository, clienteProyeccionRepository);
    }

    @Test
    void cuentaWithValidDataShouldPersistAndReturnResponse() {
        ClienteProyeccion proyeccion = ClienteProyeccion.create(1L, "Juan Perez");
        when(clienteProyeccionRepository.findByClienteId(1L)).thenReturn(Optional.of(proyeccion));
        when(cuentaRepository.existsByNumeroCuenta("ACC-001")).thenReturn(false);
        Cuenta saved = Cuenta.reconstitute(10L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.save(any())).thenReturn(saved);

        CrearCuentaRequest request = new CrearCuentaRequest(
                "ACC-001", TipoCuenta.AHORRO, new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        CuentaResponse response = useCase.ejecutar(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("ACC-001", response.getNumeroCuenta());
        verify(cuentaRepository).save(any());
    }

    @Test
    void cuentaWithNonExistentClienteShouldThrow422() {
        when(clienteProyeccionRepository.findByClienteId(99L)).thenReturn(Optional.empty());

        CrearCuentaRequest request = new CrearCuentaRequest(
                "ACC-002", TipoCuenta.AHORRO, new BigDecimal("100.00"), EstadoCuenta.ACTIVA, 99L);

        assertThrows(ClienteInactivoException.class, () -> useCase.ejecutar(request));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void cuentaWithInactiveClienteShouldThrow422() {
        ClienteProyeccion inactivo = ClienteProyeccion.reconstitute(2L, "Maria", EstadoCliente.INACTIVO);
        when(clienteProyeccionRepository.findByClienteId(2L)).thenReturn(Optional.of(inactivo));

        CrearCuentaRequest request = new CrearCuentaRequest(
                "ACC-003", TipoCuenta.AHORRO, new BigDecimal("100.00"), EstadoCuenta.ACTIVA, 2L);

        assertThrows(ClienteInactivoException.class, () -> useCase.ejecutar(request));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void cuentaWithDuplicateNumeroCuentaShouldThrow409() {
        ClienteProyeccion proyeccion = ClienteProyeccion.create(1L, "Juan Perez");
        when(clienteProyeccionRepository.findByClienteId(1L)).thenReturn(Optional.of(proyeccion));
        when(cuentaRepository.existsByNumeroCuenta("ACC-DUP")).thenReturn(true);

        CrearCuentaRequest request = new CrearCuentaRequest(
                "ACC-DUP", TipoCuenta.AHORRO, new BigDecimal("100.00"), EstadoCuenta.ACTIVA, 1L);

        assertThrows(NumeroCuentaDuplicadoException.class, () -> useCase.ejecutar(request));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void cuentaCorrienteWithBalanceLessThan50ShouldThrow400() {
        ClienteProyeccion proyeccion = ClienteProyeccion.create(1L, "Juan Perez");
        when(clienteProyeccionRepository.findByClienteId(1L)).thenReturn(Optional.of(proyeccion));
        when(cuentaRepository.existsByNumeroCuenta(anyString())).thenReturn(false);

        CrearCuentaRequest request = new CrearCuentaRequest(
                "ACC-004", TipoCuenta.CORRIENTE, new BigDecimal("10.00"), EstadoCuenta.ACTIVA, 1L);

        assertThrows(SaldoMinimoInsuficienteException.class, () -> useCase.ejecutar(request));
        verify(cuentaRepository, never()).save(any());
    }
}
