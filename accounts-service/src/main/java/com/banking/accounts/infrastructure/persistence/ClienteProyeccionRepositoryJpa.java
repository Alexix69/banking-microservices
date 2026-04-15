package com.banking.accounts.infrastructure.persistence;

import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.domain.model.EstadoCliente;
import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.infrastructure.mapper.ClienteProyeccionMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ClienteProyeccionRepositoryJpa implements ClienteProyeccionRepository {

    private final SpringDataClienteProyeccionRepository springDataRepo;
    private final ClienteProyeccionMapper clienteProyeccionMapper;

    public ClienteProyeccionRepositoryJpa(SpringDataClienteProyeccionRepository springDataRepo,
                                          ClienteProyeccionMapper clienteProyeccionMapper) {
        this.springDataRepo = springDataRepo;
        this.clienteProyeccionMapper = clienteProyeccionMapper;
    }

    @Override
    public Optional<ClienteProyeccion> findByClienteId(Long clienteId) {
        return springDataRepo.findByClienteId(clienteId)
                .map(clienteProyeccionMapper::toClienteProyeccion);
    }

    @Override
    public ClienteProyeccion save(ClienteProyeccion proyeccion) {
        ClienteProyeccionJpaEntity entity = clienteProyeccionMapper.toJpaEntity(proyeccion);
        ClienteProyeccionJpaEntity saved = springDataRepo.save(entity);
        return clienteProyeccionMapper.toClienteProyeccion(saved);
    }

    @Override
    public void desactivar(Long clienteId) {
        springDataRepo.findByClienteId(clienteId).ifPresent(e -> {
            e.setEstado(EstadoCliente.INACTIVO);
            springDataRepo.save(e);
        });
    }
}
