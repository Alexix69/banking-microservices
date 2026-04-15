package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.CrearAjusteRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.application.usecase.RegistrarAjusteUseCase;
import com.banking.accounts.domain.exception.JustificacionRequeridaException;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrarAjusteUseCaseTest {

    private MovimientoRepository movimientoRepository;
    private CuentaRepository cuentaRepository;
    private RegistrarAjusteUseCase useCase;

    @BeforeEach
    void setUp() {
        movimientoRepository = mock(MovimientoRepository.class);
        cuentaRepository = mock(CuentaRepository.class);
        useCase = new RegistrarAjusteUseCase(movimientoRepository, cuentaRepository);
    }

    @Test
    void ajusteWithValidJustificacionShouldCreateMovimiento() {
        Movimiento origen = Movimiento.reconstitute(1L, LocalDateTime.now(),
                TipoMovimiento.DEPOSITO, new BigDecimal("100.00"),
                new BigDecimal("300.00"), 2L, null, null);
        Cuenta cuenta = Cuenta.reconstitute(2L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("300.00"), new BigDecimal("300.00"), EstadoCuenta.ACTIVA, 1L);
        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));
        Movimiento ajuste = Movimiento.reconstitute(10L, LocalDateTime.now(),
                TipoMovimiento.AJUSTE, new BigDecimal("50.00"),
                new BigDecimal("350.00"), 2L, 1L, "Corrección operativa");
        when(movimientoRepository.save(any())).thenReturn(ajuste);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        CrearAjusteRequest request = new CrearAjusteRequest(1L, new BigDecimal("50.00"), "Corrección operativa");
        MovimientoResponse response = useCase.ejecutar(request);

        assertEquals(TipoMovimiento.AJUSTE, response.getTipoMovimiento());
        assertEquals("Corrección operativa", response.getJustificacion());
        verify(movimientoRepository).save(any());
    }

    @Test
    void ajusteWithoutJustificacionShouldThrowException() {
        Movimiento origen = Movimiento.reconstitute(1L, LocalDateTime.now(),
                TipoMovimiento.DEPOSITO, new BigDecimal("100.00"),
                new BigDecimal("300.00"), 2L, null, null);
        Cuenta cuenta = Cuenta.reconstitute(2L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("300.00"), new BigDecimal("300.00"), EstadoCuenta.ACTIVA, 1L);
        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));

        CrearAjusteRequest request = new CrearAjusteRequest(1L, new BigDecimal("50.00"), "");

        assertThrows(JustificacionRequeridaException.class, () -> useCase.ejecutar(request));
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void ajusteWithNonExistentMovimientoOrigenShouldThrow404() {
        when(movimientoRepository.findById(99L)).thenReturn(Optional.empty());

        CrearAjusteRequest request = new CrearAjusteRequest(99L, new BigDecimal("50.00"), "Justificacion");

        assertThrows(MovimientoNotFoundException.class, () -> useCase.ejecutar(request));
    }
}
