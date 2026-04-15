package com.banking.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataClienteProyeccionRepository extends JpaRepository<ClienteProyeccionJpaEntity, Long> {

    Optional<ClienteProyeccionJpaEntity> findByClienteId(Long clienteId);
}
