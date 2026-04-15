package com.banking.customers.domain.port;

import com.banking.customers.domain.model.Cliente;

import java.util.Optional;

public interface ClienteRepository {

    Optional<Cliente> findById(Long id);

    Cliente save(Cliente cliente);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
}
