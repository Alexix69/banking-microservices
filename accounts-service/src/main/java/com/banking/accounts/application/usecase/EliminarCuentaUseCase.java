package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.domain.exception.BusinessRuleException;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;

public class EliminarCuentaUseCase {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    public EliminarCuentaUseCase(CuentaRepository cuentaRepository,
                                  MovimientoRepository movimientoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public CuentaResponse ejecutar(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new CuentaNotFoundException(id));
        if (movimientoRepository.existsMovimientoReciente(id)) {
            throw new BusinessRuleViolationException(
                    "La cuenta no puede eliminarse porque tiene actividad en el último año");
        }
        cuenta.desactivar();
        Cuenta guardada = cuentaRepository.save(cuenta);
        return CuentaResponse.from(guardada);
    }

    private static class BusinessRuleViolationException extends BusinessRuleException {
        BusinessRuleViolationException(String message) {
            super(message);
        }
    }
}
