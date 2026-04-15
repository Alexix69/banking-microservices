package com.banking.accounts.infrastructure.mapper;

import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.infrastructure.persistence.ClienteProyeccionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClienteProyeccionMapper {

    default ClienteProyeccion toClienteProyeccion(ClienteProyeccionJpaEntity entity) {
        return ClienteProyeccion.reconstitute(
                entity.getClienteId(),
                entity.getNombre(),
                entity.getEstado()
        );
    }

    ClienteProyeccionJpaEntity toJpaEntity(ClienteProyeccion proyeccion);
}
