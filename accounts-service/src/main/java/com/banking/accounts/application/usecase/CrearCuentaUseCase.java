package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.CrearCuentaRequest;
import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.domain.exception.ClienteInactivoException;
import com.banking.accounts.domain.exception.NumeroCuentaDuplicadoException;
import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;

public class CrearCuentaUseCase {

    private final CuentaRepository cuentaRepository;
    private final ClienteProyeccionRepository clienteProyeccionRepository;

    public CrearCuentaUseCase(CuentaRepository cuentaRepository,
                              ClienteProyeccionRepository clienteProyeccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteProyeccionRepository = clienteProyeccionRepository;
    }

    public CuentaResponse ejecutar(CrearCuentaRequest request) {
        ClienteProyeccion proyeccion = buscarClienteActivo(request.getClienteId());
        if (cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new NumeroCuentaDuplicadoException(request.getNumeroCuenta());
        }
        Cuenta cuenta = Cuenta.create(request.getNumeroCuenta(), request.getTipo(),
                request.getSaldoInicial(), request.getEstado(), proyeccion);
        return CuentaResponse.from(cuentaRepository.save(cuenta));
    }

    private ClienteProyeccion buscarClienteActivo(Long clienteId) {
        ClienteProyeccion proyeccion = clienteProyeccionRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ClienteInactivoException(
                        "El cliente referenciado no existe o está inactivo"));
        if (!proyeccion.estaActivo()) {
            throw new ClienteInactivoException("El cliente referenciado no existe o está inactivo");
        }
        return proyeccion;
    }
}
