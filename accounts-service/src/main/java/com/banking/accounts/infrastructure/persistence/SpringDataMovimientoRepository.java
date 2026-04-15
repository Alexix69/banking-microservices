package com.banking.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataMovimientoRepository extends JpaRepository<MovimientoJpaEntity, Long> {

    List<MovimientoJpaEntity> findByCuentaIdInAndFechaBetween(
            List<Long> cuentaIds,
            LocalDateTime inicio,
            LocalDateTime fin);

    @Query(value = "SELECT COALESCE(SUM(ABS(m.valor)), 0) " +
                   "FROM movimiento m " +
                   "INNER JOIN cuenta c ON m.cuenta_id = c.id " +
                   "WHERE c.cliente_id = :clienteId " +
                   "AND m.tipo = 'RETIRO' " +
                   "AND DATE(m.fecha) = CURRENT_DATE",
           nativeQuery = true)
    BigDecimal sumRetirosDiariosByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM MovimientoJpaEntity m " +
           "WHERE m.cuentaId = :cuentaId AND m.fecha >= :unAnioAtras")
    boolean existsMovimientoReciente(@Param("cuentaId") Long cuentaId,
                                     @Param("unAnioAtras") LocalDateTime unAnioAtras);
}
