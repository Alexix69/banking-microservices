package com.banking.accounts.domain.port;

import com.banking.accounts.domain.model.Movimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository {

    Optional<Movimiento> findById(Long id);

    Movimiento save(Movimiento movimiento);

    BigDecimal sumRetirosDiariosByClienteId(Long clienteId);

    boolean existsMovimientoReciente(Long cuentaId);

    List<Movimiento> findByCuentaIdInAndFechaBetween(List<Long> cuentaIds,
                                                      LocalDateTime inicio,
                                                      LocalDateTime fin);
}
