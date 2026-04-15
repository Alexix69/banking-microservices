package com.banking.accounts.infrastructure.persistence;

import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.port.MovimientoRepository;
import com.banking.accounts.infrastructure.mapper.MovimientoMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MovimientoRepositoryJpa implements MovimientoRepository {

    private final SpringDataMovimientoRepository springDataRepo;
    private final MovimientoMapper movimientoMapper;

    public MovimientoRepositoryJpa(SpringDataMovimientoRepository springDataRepo, MovimientoMapper movimientoMapper) {
        this.springDataRepo = springDataRepo;
        this.movimientoMapper = movimientoMapper;
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return springDataRepo.findById(id).map(movimientoMapper::toMovimiento);
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        MovimientoJpaEntity entity = movimientoMapper.toJpaEntity(movimiento);
        MovimientoJpaEntity saved = springDataRepo.save(entity);
        return movimientoMapper.toMovimiento(saved);
    }

    @Override
    public BigDecimal sumRetirosDiariosByClienteId(Long clienteId) {
        BigDecimal result = springDataRepo.sumRetirosDiariosByClienteId(clienteId);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public boolean existsMovimientoReciente(Long cuentaId) {
        return springDataRepo.existsMovimientoReciente(cuentaId, LocalDateTime.now().minusYears(1));
    }

    @Override
    public List<Movimiento> findByCuentaIdInAndFechaBetween(List<Long> cuentaIds,
                                                             LocalDateTime inicio,
                                                             LocalDateTime fin) {
        return springDataRepo.findByCuentaIdInAndFechaBetween(cuentaIds, inicio, fin)
                .stream()
                .map(movimientoMapper::toMovimiento)
                .toList();
    }
}
