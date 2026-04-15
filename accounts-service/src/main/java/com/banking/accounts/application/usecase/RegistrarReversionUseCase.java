package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.CrearReversionRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;

import java.math.BigDecimal;

public class RegistrarReversionUseCase {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public RegistrarReversionUseCase(MovimientoRepository movimientoRepository,
                                      CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public MovimientoResponse ejecutar(CrearReversionRequest request) {
        Movimiento movimientoOrigen = movimientoRepository.findById(request.getMovimientoOrigenId())
                .orElseThrow(() -> new MovimientoNotFoundException(request.getMovimientoOrigenId()));
        BigDecimal valorReversion = movimientoOrigen.getValor().negate();
        Cuenta cuenta = cuentaRepository.findById(movimientoOrigen.getCuentaId())
                .orElseThrow(() -> new CuentaNotFoundException(movimientoOrigen.getCuentaId()));
        BigDecimal saldoResultante = cuenta.getSaldoDisponible().add(valorReversion);
        Movimiento reversion = Movimiento.create(TipoMovimiento.REVERSION, valorReversion,
                saldoResultante, cuenta.getId(), request.getMovimientoOrigenId(), null);
        cuenta.aplicarMovimiento(valorReversion);
        Movimiento guardado = movimientoRepository.save(reversion);
        cuentaRepository.save(cuenta);
        return MovimientoResponse.from(guardado);
    }
}
