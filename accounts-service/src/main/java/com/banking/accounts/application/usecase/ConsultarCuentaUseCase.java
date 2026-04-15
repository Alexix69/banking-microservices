package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.port.CuentaRepository;

public class ConsultarCuentaUseCase {

    private final CuentaRepository cuentaRepository;

    public ConsultarCuentaUseCase(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public CuentaResponse ejecutar(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new CuentaNotFoundException(id));
        return CuentaResponse.from(cuenta);
    }
}
