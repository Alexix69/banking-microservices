package com.banking.accounts.infrastructure.persistence;

import com.banking.accounts.domain.model.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCuentaRepository extends JpaRepository<CuentaJpaEntity, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<CuentaJpaEntity> findByClienteId(Long clienteId);

    List<CuentaJpaEntity> findByClienteIdAndEstado(Long clienteId, EstadoCuenta estado);
}
