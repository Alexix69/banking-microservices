package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.ReporteItemResponse;
import com.banking.accounts.application.usecase.GenerarReporteUseCase;
import com.banking.accounts.domain.exception.BusinessRuleException;
import com.banking.accounts.domain.exception.ResourceNotFoundException;
import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GenerarReporteUseCaseTest {

    private ClienteProyeccionRepository clienteProyeccionRepository;
    private CuentaRepository cuentaRepository;
    private MovimientoRepository movimientoRepository;
    private GenerarReporteUseCase useCase;

    @BeforeEach
    void setUp() {
        clienteProyeccionRepository = mock(ClienteProyeccionRepository.class);
        cuentaRepository = mock(CuentaRepository.class);
        movimientoRepository = mock(MovimientoRepository.class);
        useCase = new GenerarReporteUseCase(clienteProyeccionRepository, cuentaRepository, movimientoRepository);
    }

    @Test
    void reporteWithValidClienteAndDateRangeShouldReturnItems() {
        ClienteProyeccion proyeccion = ClienteProyeccion.create(1L, "Juan Perez");
        Cuenta cuenta = Cuenta.reconstitute(10L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("500.00"), new BigDecimal("400.00"), EstadoCuenta.ACTIVA, 1L);
        Movimiento mov = Movimiento.reconstitute(100L, LocalDateTime.now(), TipoMovimiento.RETIRO,
                new BigDecimal("-100.00"), new BigDecimal("400.00"), 10L, null, null);
        when(clienteProyeccionRepository.findByClienteId(1L)).thenReturn(Optional.of(proyeccion));
        when(cuentaRepository.findAllByClienteId(1L)).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuentaIdInAndFechaBetween(anyList(), any(), any()))
                .thenReturn(List.of(mov));

        List<ReporteItemResponse> result = useCase.ejecutar(1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(1, result.size());
    }

    @Test
    void reporteWithNonExistentClienteShouldThrowException() {
        when(clienteProyeccionRepository.findByClienteId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                useCase.ejecutar(99L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
    }

    @Test
    void reporteWithNoMovimientosShouldReturnEmptyList() {
        ClienteProyeccion proyeccion = ClienteProyeccion.create(1L, "Juan Perez");
        Cuenta cuenta = Cuenta.reconstitute(10L, "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("500.00"), new BigDecimal("500.00"), EstadoCuenta.ACTIVA, 1L);
        when(clienteProyeccionRepository.findByClienteId(1L)).thenReturn(Optional.of(proyeccion));
        when(cuentaRepository.findAllByClienteId(1L)).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuentaIdInAndFechaBetween(anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<ReporteItemResponse> result = useCase.ejecutar(1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertTrue(result.isEmpty());
    }

    @Test
    void reporteWithInvalidDateRangeShouldThrowException() {
        assertThrows(BusinessRuleException.class, () ->
                useCase.ejecutar(1L, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void reporteShouldIncludeAllRequiredFields() {
        ClienteProyeccion proyeccion = ClienteProyeccion.create(1L, "Maria Lopez");
        Cuenta cuenta = Cuenta.reconstitute(20L, "ACC-002", TipoCuenta.CORRIENTE,
                new BigDecimal("200.00"), new BigDecimal("150.00"), EstadoCuenta.ACTIVA, 1L);
        Movimiento mov = Movimiento.reconstitute(200L, LocalDateTime.of(2026, 3, 15, 10, 0),
                TipoMovimiento.DEPOSITO, new BigDecimal("50.00"),
                new BigDecimal("150.00"), 20L, null, null);
        when(clienteProyeccionRepository.findByClienteId(1L)).thenReturn(Optional.of(proyeccion));
        when(cuentaRepository.findAllByClienteId(1L)).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuentaIdInAndFechaBetween(anyList(), any(), any()))
                .thenReturn(List.of(mov));

        List<ReporteItemResponse> result = useCase.ejecutar(1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(1, result.size());
        ReporteItemResponse item = result.get(0);
        assertEquals("Maria Lopez", item.getCliente());
        assertEquals("ACC-002", item.getNumeroCuenta());
        assertEquals(TipoCuenta.CORRIENTE, item.getTipoCuenta());
        assertEquals(new BigDecimal("200.00"), item.getSaldoInicial());
        assertEquals(EstadoCuenta.ACTIVA, item.getEstado());
        assertEquals(new BigDecimal("50.00"), item.getMovimiento());
        assertEquals(new BigDecimal("150.00"), item.getSaldoDisponible());
    }
}
