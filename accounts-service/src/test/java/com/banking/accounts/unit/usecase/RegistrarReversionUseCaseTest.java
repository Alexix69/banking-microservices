package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.CrearReversionRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.application.usecase.RegistrarReversionUseCase;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.exception.SaldoInsuficienteException;
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

class RegistrarReversionUseCaseTest {

    private MovimientoRepository movimientoRepository;
    private CuentaRepository cuentaRepository;
    private RegistrarReversionUseCase useCase;

    @BeforeEach
    void setUp() {
        movimientoRepository = mock(MovimientoRepository.class);
        cuentaRepository = mock(CuentaRepository.class);
        useCase = new RegistrarReversionUseCase(movimientoRepository, cuentaRepository);
    }

    @Test
    void reversionShouldCreateMovimientoWithOppositeValue() {
        Movimiento origen = Movimiento.reconstitute(1L, LocalDateTime.now(),
                TipoMovimiento.DEPOSITO, new BigDecimal("100.00"),
                new BigDecimal("300.00"), 2L, null, null);
        Cuenta cuenta = Cuenta.reconstitute(2L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("300.00"), new BigDecimal("300.00"), EstadoCuenta.ACTIVA, 1L);
        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));
        Movimiento reversion = Movimiento.reconstitute(20L, LocalDateTime.now(),
                TipoMovimiento.REVERSION, new BigDecimal("-100.00"),
                new BigDecimal("200.00"), 2L, 1L, null);
        when(movimientoRepository.save(any())).thenReturn(reversion);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        MovimientoResponse response = useCase.ejecutar(new CrearReversionRequest(1L));

        assertEquals(TipoMovimiento.REVERSION, response.getTipoMovimiento());
        assertEquals(new BigDecimal("-100.00"), response.getValor());
    }

    @Test
    void reversionOfDepositoShouldDecreaseSaldo() {
        Movimiento deposito = Movimiento.reconstitute(2L, LocalDateTime.now(),
                TipoMovimiento.DEPOSITO, new BigDecimal("200.00"),
                new BigDecimal("500.00"), 3L, null, null);
        Cuenta cuenta = Cuenta.reconstitute(3L, "ACC-002", TipoCuenta.AHORRO,
                new BigDecimal("500.00"), new BigDecimal("500.00"), EstadoCuenta.ACTIVA, 1L);
        when(movimientoRepository.findById(2L)).thenReturn(Optional.of(deposito));
        when(cuentaRepository.findById(3L)).thenReturn(Optional.of(cuenta));
        Movimiento reversion = Movimiento.reconstitute(21L, LocalDateTime.now(),
                TipoMovimiento.REVERSION, new BigDecimal("-200.00"),
                new BigDecimal("300.00"), 3L, 2L, null);
        when(movimientoRepository.save(any())).thenReturn(reversion);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        MovimientoResponse response = useCase.ejecutar(new CrearReversionRequest(2L));

        assertEquals(new BigDecimal("300.00"), response.getSaldoResultante());
    }

    @Test
    void reversionOfRetiroShouldIncreaseSaldo() {
        Movimiento retiro = Movimiento.reconstitute(3L, LocalDateTime.now(),
                TipoMovimiento.RETIRO, new BigDecimal("-100.00"),
                new BigDecimal("100.00"), 4L, null, null);
        Cuenta cuenta = Cuenta.reconstitute(4L, "ACC-003", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("100.00"), EstadoCuenta.ACTIVA, 1L);
        when(movimientoRepository.findById(3L)).thenReturn(Optional.of(retiro));
        when(cuentaRepository.findById(4L)).thenReturn(Optional.of(cuenta));
        Movimiento reversion = Movimiento.reconstitute(22L, LocalDateTime.now(),
                TipoMovimiento.REVERSION, new BigDecimal("100.00"),
                new BigDecimal("200.00"), 4L, 3L, null);
        when(movimientoRepository.save(any())).thenReturn(reversion);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        MovimientoResponse response = useCase.ejecutar(new CrearReversionRequest(3L));

        assertEquals(new BigDecimal("100.00"), response.getValor());
        assertEquals(new BigDecimal("200.00"), response.getSaldoResultante());
    }

    @Test
    void reversionWithNonExistentMovimientoOrigenShouldThrow404() {
        when(movimientoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MovimientoNotFoundException.class, () ->
                useCase.ejecutar(new CrearReversionRequest(99L)));
    }

    @Test
    void reversionWhenCurrentBalanceLowerThanOriginalShouldThrow() {
        Movimiento movimientoOrigen = Movimiento.reconstitute(1L, LocalDateTime.now(),
                TipoMovimiento.DEPOSITO, new BigDecimal("500.00"),
                new BigDecimal("500.00"), 2L, null, null);
        Cuenta cuenta = Cuenta.reconstitute(2L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("500.00"), new BigDecimal("100.00"), EstadoCuenta.ACTIVA, 1L);
        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(movimientoOrigen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));

        assertThrows(SaldoInsuficienteException.class, () ->
                useCase.ejecutar(new CrearReversionRequest(1L)));
        verify(movimientoRepository, never()).save(any());
    }
}
