package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.ActualizarCuentaRequest;
import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import org.springframework.stereotype.Service;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.port.CuentaRepository;

@Service
public class ActualizarCuentaUseCase {

    private final CuentaRepository cuentaRepository;

    public ActualizarCuentaUseCase(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public CuentaResponse ejecutar(Long id, ActualizarCuentaRequest request) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new CuentaNotFoundException(id));
        Cuenta actualizada = Cuenta.reconstitute(
                cuenta.getId(),
                request.getNumeroCuenta() != null ? request.getNumeroCuenta() : cuenta.getNumeroCuenta(),
                request.getTipo() != null ? request.getTipo() : cuenta.getTipo(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldoDisponible(),
                request.getEstado() != null ? request.getEstado() : cuenta.getEstado(),
                cuenta.getClienteId()
        );
        Cuenta guardada = cuentaRepository.save(actualizada);
        return CuentaResponse.from(guardada);
    }
}
