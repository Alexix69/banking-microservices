package com.banking.accounts.infrastructure.mapper;

import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.infrastructure.persistence.MovimientoJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {

    default Movimiento toMovimiento(MovimientoJpaEntity entity) {
        return Movimiento.reconstitute(
                entity.getId(),
                entity.getFecha(),
                entity.getTipoMovimiento(),
                entity.getValor(),
                entity.getSaldoResultante(),
                entity.getCuentaId(),
                entity.getMovimientoOrigenId(),
                entity.getJustificacion()
        );
    }

    MovimientoJpaEntity toJpaEntity(Movimiento movimiento);
}
