package com.banking.customers.infrastructure.mapper;

import com.banking.customers.domain.model.Cliente;
import com.banking.customers.infrastructure.persistence.ClienteJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    default Cliente toCliente(ClienteJpaEntity entity) {
        return Cliente.reconstitute(
                entity.getId(),
                entity.getNombre(),
                entity.getGenero(),
                entity.getEdad(),
                entity.getIdentificacion(),
                entity.getDireccion(),
                entity.getTelefono(),
                entity.getContrasena(),
                entity.getEstado()
        );
    }

    ClienteJpaEntity toJpaEntity(Cliente cliente);
}
