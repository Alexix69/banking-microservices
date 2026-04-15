package com.banking.accounts.unit.application;

import com.banking.accounts.application.dto.CrearAjusteRequest;
import com.banking.accounts.application.dto.CrearCuentaRequest;
import com.banking.accounts.application.dto.CrearMovimientoRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.application.dto.ReporteItemResponse;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.domain.model.TipoMovimiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoStructureTest {

    @Test
    void crearCuentaRequestShouldHaveAllRequiredFields() {
        CrearCuentaRequest request = new CrearCuentaRequest(
                "ACC-001", TipoCuenta.AHORRO, new BigDecimal("100.00"),
                EstadoCuenta.ACTIVA, 1L);

        assertEquals("ACC-001", request.getNumeroCuenta());
        assertEquals(TipoCuenta.AHORRO, request.getTipo());
        assertEquals(new BigDecimal("100.00"), request.getSaldoInicial());
        assertEquals(EstadoCuenta.ACTIVA, request.getEstado());
        assertEquals(1L, request.getClienteId());
    }

    @Test
    void crearMovimientoRequestShouldAcceptPositiveAndNegativeValues() {
        CrearMovimientoRequest deposito = new CrearMovimientoRequest(1L, new BigDecimal("200.00"));
        CrearMovimientoRequest retiro = new CrearMovimientoRequest(1L, new BigDecimal("-150.00"));

        assertTrue(deposito.getValor().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(retiro.getValor().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void crearAjusteRequestShouldRequireJustificacion() {
        CrearAjusteRequest request = new CrearAjusteRequest(
                10L, new BigDecimal("50.00"), "Corrección por error operativo");

        assertNotNull(request.getJustificacion());
        assertFalse(request.getJustificacion().isBlank());
        assertEquals("Corrección por error operativo", request.getJustificacion());
    }

    @Test
    void movimientoResponseShouldIncludeJustificacion() {
        Movimiento movimiento = Movimiento.reconstitute(
                1L, LocalDateTime.now(), TipoMovimiento.AJUSTE,
                new BigDecimal("100.00"), new BigDecimal("600.00"),
                2L, 5L, "Ajuste por conciliación");

        MovimientoResponse response = MovimientoResponse.from(movimiento);

        assertEquals("Ajuste por conciliación", response.getJustificacion());
        assertEquals(TipoMovimiento.AJUSTE, response.getTipoMovimiento());
        assertEquals(5L, response.getMovimientoOrigenId());
    }

    @Test
    void reporteItemResponseShouldHaveAllReportFields() {
        LocalDateTime fecha = LocalDateTime.now();
        ReporteItemResponse item = new ReporteItemResponse(
                fecha, "Juan Perez", "ACC-001", TipoCuenta.AHORRO,
                new BigDecimal("500.00"), EstadoCuenta.ACTIVA,
                new BigDecimal("-100.00"), new BigDecimal("400.00"));

        assertEquals(fecha, item.getFecha());
        assertEquals("Juan Perez", item.getCliente());
        assertEquals("ACC-001", item.getNumeroCuenta());
        assertEquals(TipoCuenta.AHORRO, item.getTipoCuenta());
        assertEquals(new BigDecimal("500.00"), item.getSaldoInicial());
        assertEquals(EstadoCuenta.ACTIVA, item.getEstado());
        assertEquals(new BigDecimal("-100.00"), item.getMovimiento());
        assertEquals(new BigDecimal("400.00"), item.getSaldoDisponible());
    }
}
