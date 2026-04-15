package com.banking.customers.infrastructure.persistence;

import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.infrastructure.mapper.CustomerMapper;

import java.util.Optional;

public class CustomerRepositoryJpa implements ClienteRepository {

    private final SpringDataClienteRepository springDataRepo;
    private final CustomerMapper customerMapper;

    public CustomerRepositoryJpa(SpringDataClienteRepository springDataRepo, CustomerMapper customerMapper) {
        this.springDataRepo = springDataRepo;
        this.customerMapper = customerMapper;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return springDataRepo.findById(id).map(customerMapper::toCliente);
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteJpaEntity entity = customerMapper.toJpaEntity(cliente);
        ClienteJpaEntity saved = springDataRepo.save(entity);
        return customerMapper.toCliente(saved);
    }

    @Override
    public boolean existsByIdentificacion(String identificacion) {
        return springDataRepo.existsByIdentificacion(identificacion);
    }

    @Override
    public boolean existsByIdentificacionAndIdNot(String identificacion, Long id) {
        return springDataRepo.existsByIdentificacionAndIdNot(identificacion, id);
    }
}
