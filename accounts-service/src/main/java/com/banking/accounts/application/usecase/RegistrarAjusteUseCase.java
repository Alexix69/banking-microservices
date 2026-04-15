package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.CrearAjusteRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import org.springframework.stereotype.Service;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.exception.JustificacionRequeridaException;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;

import java.math.BigDecimal;

@Service
public class RegistrarAjusteUseCase {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public RegistrarAjusteUseCase(MovimientoRepository movimientoRepository,
                                   CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public MovimientoResponse ejecutar(CrearAjusteRequest request) {
        Movimiento movimientoOrigen = movimientoRepository.findById(request.getMovimientoOrigenId())
                .orElseThrow(() -> new MovimientoNotFoundException(request.getMovimientoOrigenId()));
        validarJustificacion(request.getJustificacion());
        Cuenta cuenta = cuentaRepository.findById(movimientoOrigen.getCuentaId())
                .orElseThrow(() -> new CuentaNotFoundException(movimientoOrigen.getCuentaId()));
        BigDecimal valor = request.getValor();
        BigDecimal saldoResultante = cuenta.getSaldoDisponible().add(valor);
        Movimiento ajuste = Movimiento.create(TipoMovimiento.AJUSTE, valor, saldoResultante,
                cuenta.getId(), request.getMovimientoOrigenId(), request.getJustificacion());
        cuenta.aplicarMovimiento(valor);
        Movimiento guardado = movimientoRepository.save(ajuste);
        cuentaRepository.save(cuenta);
        return MovimientoResponse.from(guardado);
    }

    private void validarJustificacion(String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new JustificacionRequeridaException();
        }
    }
}
