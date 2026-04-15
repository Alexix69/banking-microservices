package com.banking.accounts.domain.port;

import com.banking.accounts.domain.model.ClienteProyeccion;

import java.util.Optional;

public interface ClienteProyeccionRepository {

    Optional<ClienteProyeccion> findByClienteId(Long clienteId);

    ClienteProyeccion save(ClienteProyeccion proyeccion);

    void desactivar(Long clienteId);
}
