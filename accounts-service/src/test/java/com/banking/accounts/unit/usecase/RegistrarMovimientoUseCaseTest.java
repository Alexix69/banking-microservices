package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.CrearMovimientoRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.application.usecase.RegistrarMovimientoUseCase;
import com.banking.accounts.domain.exception.CuentaInactivaException;
import com.banking.accounts.domain.exception.LimiteDiarioExcedidoException;
import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import com.banking.accounts.domain.exception.ValorMovimientoInvalidoException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import com.banking.accounts.domain.validator.CuentaActivaValidator;
import com.banking.accounts.domain.validator.LimiteDiarioValidator;
import com.banking.accounts.domain.validator.MovimientoValidator;
import com.banking.accounts.domain.validator.SaldoInsuficienteValidator;
import com.banking.accounts.domain.validator.ValorCeroValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class RegistrarMovimientoUseCaseTest {

    private CuentaRepository cuentaRepository;
    private MovimientoRepository movimientoRepository;
    private RegistrarMovimientoUseCase useCase;

    @BeforeEach
    void setUp() {
        cuentaRepository = mock(CuentaRepository.class);
        movimientoRepository = mock(MovimientoRepository.class);
        List<MovimientoValidator> validators = List.of(
                new CuentaActivaValidator(),
                new ValorCeroValidator(),
                new SaldoInsuficienteValidator(),
                new LimiteDiarioValidator()
        );
        useCase = new RegistrarMovimientoUseCase(cuentaRepository, movimientoRepository, validators);
    }

    @Test
    void depositoOnActiveCuentaShouldIncreaseSaldo() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(1L)).thenReturn(BigDecimal.ZERO);
        Movimiento saved = Movimiento.reconstitute(10L, LocalDateTime.now(), TipoMovimiento.DEPOSITO,
                new BigDecimal("100.00"), new BigDecimal("300.00"), 1L, null, null);
        when(movimientoRepository.save(any())).thenReturn(saved);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        MovimientoResponse response = useCase.ejecutar(
                new CrearMovimientoRequest(1L, new BigDecimal("100.00")));

        assertEquals(TipoMovimiento.DEPOSITO, response.getTipoMovimiento());
        assertEquals(new BigDecimal("300.00"), response.getSaldoResultante());
    }

    @Test
    void retiroWithSufficientBalanceShouldDecreaseSaldo() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("300.00"), new BigDecimal("300.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(1L)).thenReturn(BigDecimal.ZERO);
        Movimiento saved = Movimiento.reconstitute(11L, LocalDateTime.now(), TipoMovimiento.RETIRO,
                new BigDecimal("-100.00"), new BigDecimal("200.00"), 1L, null, null);
        when(movimientoRepository.save(any())).thenReturn(saved);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        MovimientoResponse response = useCase.ejecutar(
                new CrearMovimientoRequest(1L, new BigDecimal("-100.00")));

        assertEquals(TipoMovimiento.RETIRO, response.getTipoMovimiento());
    }

    @Test
    void retiroWithInsufficientBalanceShouldThrowSaldoInsuficiente() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("50.00"), new BigDecimal("50.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(1L)).thenReturn(BigDecimal.ZERO);

        assertThrows(SaldoInsuficienteException.class, () ->
                useCase.ejecutar(new CrearMovimientoRequest(1L, new BigDecimal("-200.00"))));
    }

    @Test
    void retiroExceedingDailyLimitShouldThrowLimiteDiario() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("1000.00"), new BigDecimal("1000.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(1L)).thenReturn(new BigDecimal("400.00"));

        assertThrows(LimiteDiarioExcedidoException.class, () ->
                useCase.ejecutar(new CrearMovimientoRequest(1L, new BigDecimal("-200.00"))));
    }

    @Test
    void movimientoWithZeroValueShouldThrowValorCero() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(1L)).thenReturn(BigDecimal.ZERO);

        assertThrows(ValorMovimientoInvalidoException.class, () ->
                useCase.ejecutar(new CrearMovimientoRequest(1L, BigDecimal.ZERO)));
    }

    @Test
    void movimientoOnInactiveCuentaShouldThrowCuentaInactiva() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.INACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(anyLong())).thenReturn(BigDecimal.ZERO);

        assertThrows(CuentaInactivaException.class, () ->
                useCase.ejecutar(new CrearMovimientoRequest(1L, new BigDecimal("100.00"))));
    }

    @Test
    void depositoShouldNotAffectDailyLimit() {
        Cuenta cuenta = Cuenta.reconstitute(1L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("200.00"), new BigDecimal("200.00"), EstadoCuenta.ACTIVA, 1L);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.sumRetirosDiariosByClienteId(1L)).thenReturn(new BigDecimal("490.00"));
        Movimiento saved = Movimiento.reconstitute(12L, LocalDateTime.now(), TipoMovimiento.DEPOSITO,
                new BigDecimal("100.00"), new BigDecimal("300.00"), 1L, null, null);
        when(movimientoRepository.save(any())).thenReturn(saved);
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        MovimientoResponse response = useCase.ejecutar(
                new CrearMovimientoRequest(1L, new BigDecimal("100.00")));

        assertEquals(TipoMovimiento.DEPOSITO, response.getTipoMovimiento());
    }
}
