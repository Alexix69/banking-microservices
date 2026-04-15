package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.ReporteItemResponse;
import com.banking.accounts.domain.exception.BusinessRuleException;
import com.banking.accounts.domain.exception.ClienteNotFoundException;
import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerarReporteUseCase {

    private final ClienteProyeccionRepository clienteProyeccionRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    public GenerarReporteUseCase(ClienteProyeccionRepository clienteProyeccionRepository,
                                  CuentaRepository cuentaRepository,
                                  MovimientoRepository movimientoRepository) {
        this.clienteProyeccionRepository = clienteProyeccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<ReporteItemResponse> ejecutar(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        validarFechas(fechaInicio, fechaFin);
        ClienteProyeccion proyeccion = clienteProyeccionRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ClienteNotFoundException(clienteId));
        List<Cuenta> cuentas = cuentaRepository.findAllByClienteId(clienteId);
        if (cuentas.isEmpty()) {
            return Collections.emptyList();
        }
        List<Movimiento> movimientos = buscarMovimientos(cuentas, fechaInicio, fechaFin);
        if (movimientos.isEmpty()) {
            return Collections.emptyList();
        }
        return construirReporte(movimientos, cuentas, proyeccion);
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new FechaRangoInvalidoException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private List<Movimiento> buscarMovimientos(List<Cuenta> cuentas,
                                                LocalDate inicio, LocalDate fin) {
        List<Long> cuentaIds = cuentas.stream().map(Cuenta::getId).toList();
        return movimientoRepository.findByCuentaIdInAndFechaBetween(
                cuentaIds, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    private List<ReporteItemResponse> construirReporte(List<Movimiento> movimientos,
                                                        List<Cuenta> cuentas,
                                                        ClienteProyeccion proyeccion) {
        Map<Long, Cuenta> cuentasPorId = cuentas.stream()
                .collect(Collectors.toMap(Cuenta::getId, c -> c));
        return movimientos.stream()
                .map(m -> new ReporteItemResponse(
                        m.getFecha(), proyeccion.getNombre(),
                        cuentasPorId.get(m.getCuentaId()).getNumeroCuenta(),
                        cuentasPorId.get(m.getCuentaId()).getTipo(),
                        cuentasPorId.get(m.getCuentaId()).getSaldoInicial(),
                        cuentasPorId.get(m.getCuentaId()).getEstado(),
                        m.getValor(), m.getSaldoResultante()))
                .toList();
    }

    private static class FechaRangoInvalidoException extends BusinessRuleException {
        FechaRangoInvalidoException(String message) {
            super(message);
        }
    }
}
