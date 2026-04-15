package com.banking.customers.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataClienteRepository extends JpaRepository<ClienteJpaEntity, Long> {

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
}
