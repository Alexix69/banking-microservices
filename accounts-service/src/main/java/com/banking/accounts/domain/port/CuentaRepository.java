package com.banking.accounts.domain.port;

import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository {

    Optional<Cuenta> findById(Long id);

    Cuenta save(Cuenta cuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteIdAndEstado(Long clienteId, EstadoCuenta estado);

    void desactivarTodasPorClienteId(Long clienteId);
}
