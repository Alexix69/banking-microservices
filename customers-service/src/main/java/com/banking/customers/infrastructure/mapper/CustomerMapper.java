package com.banking.customers.infrastructure.mapper;

import com.banking.customers.domain.model.Cliente;
import com.banking.customers.infrastructure.persistence.ClienteJpaEntity;

public class CustomerMapper {

    public Cliente toCliente(ClienteJpaEntity entity) {
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

    public ClienteJpaEntity toJpaEntity(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(cliente.getId());
        entity.setNombre(cliente.getNombre());
        entity.setGenero(cliente.getGenero());
        entity.setEdad(cliente.getEdad());
        entity.setIdentificacion(cliente.getIdentificacion());
        entity.setDireccion(cliente.getDireccion());
        entity.setTelefono(cliente.getTelefono());
        entity.setContrasena(cliente.getContrasena());
        entity.setEstado(cliente.getEstado());
        return entity;
    }
}
