package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.application.usecase.EliminarCuentaUseCase;
import com.banking.accounts.domain.exception.BusinessRuleException;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EliminarCuentaUseCaseTest {

    private CuentaRepository cuentaRepository;
    private MovimientoRepository movimientoRepository;
    private EliminarCuentaUseCase useCase;

    @BeforeEach
    void setUp() {
        cuentaRepository = mock(CuentaRepository.class);
        movimientoRepository = mock(MovimientoRepository.class);
        useCase = new EliminarCuentaUseCase(cuentaRepository, movimientoRepository);
    }

    @Test
    void cuentaWithoutRecentActivityShouldDeactivate() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.existsMovimientoReciente(1L)).thenReturn(false);
        Cuenta desactivada = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.INACTIVA, 1L);
        when(cuentaRepository.save(any())).thenReturn(desactivada);

        CuentaResponse response = useCase.ejecutar(1L);

        assertEquals(EstadoCuenta.INACTIVA, response.getEstado());
        verify(cuentaRepository).save(any());
    }

    @Test
    void cuentaWithRecentActivityShouldThrow409() {
        Cuenta cuenta = Cuenta.reconstitute(2L, "ACC-002", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.existsMovimientoReciente(2L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> useCase.ejecutar(2L));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void nonExistentCuentaShouldThrowCuentaNotFoundException() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CuentaNotFoundException.class, () -> useCase.ejecutar(99L));
        verify(cuentaRepository, never()).save(any());
    }
}
