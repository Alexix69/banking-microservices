package com.banking.accounts.infrastructure.persistence;

import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.infrastructure.mapper.AccountMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryJpa implements CuentaRepository {

    private final SpringDataCuentaRepository springDataRepo;
    private final AccountMapper accountMapper;

    public AccountRepositoryJpa(SpringDataCuentaRepository springDataRepo, AccountMapper accountMapper) {
        this.springDataRepo = springDataRepo;
        this.accountMapper = accountMapper;
    }

    @Override
    public Optional<Cuenta> findById(Long id) {
        return springDataRepo.findById(id).map(accountMapper::toCuenta);
    }

    @Override
    public Cuenta save(Cuenta cuenta) {
        CuentaJpaEntity entity = accountMapper.toJpaEntity(cuenta);
        CuentaJpaEntity saved = springDataRepo.save(entity);
        return accountMapper.toCuenta(saved);
    }

    @Override
    public boolean existsByNumeroCuenta(String numeroCuenta) {
        return springDataRepo.existsByNumeroCuenta(numeroCuenta);
    }

    @Override
    public List<Cuenta> findByClienteIdAndEstado(Long clienteId, EstadoCuenta estado) {
        return springDataRepo.findByClienteIdAndEstado(clienteId, estado)
                .stream()
                .map(accountMapper::toCuenta)
                .toList();
    }

    @Override
    public List<Cuenta> findAllByClienteId(Long clienteId) {
        return springDataRepo.findByClienteId(clienteId)
                .stream()
                .map(accountMapper::toCuenta)
                .toList();
    }

    @Override
    public void desactivarTodasPorClienteId(Long clienteId) {
        springDataRepo.findByClienteId(clienteId).forEach(e -> {
            e.setEstado(EstadoCuenta.INACTIVA);
            springDataRepo.save(e);
        });
    }
}
