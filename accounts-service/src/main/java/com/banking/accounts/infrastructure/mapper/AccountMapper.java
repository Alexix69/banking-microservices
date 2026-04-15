package com.banking.accounts.infrastructure.mapper;

import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.infrastructure.persistence.CuentaJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    default Cuenta toCuenta(CuentaJpaEntity entity) {
        return Cuenta.reconstitute(
                entity.getId(),
                entity.getNumeroCuenta(),
                entity.getTipo(),
                entity.getSaldoInicial(),
                entity.getSaldoDisponible(),
                entity.getEstado(),
                entity.getClienteId()
        );
    }

    CuentaJpaEntity toJpaEntity(Cuenta cuenta);
}
