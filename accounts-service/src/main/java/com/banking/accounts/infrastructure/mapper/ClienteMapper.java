package com.banking.accounts.infrastructure.mapper;

import com.banking.accounts.domain.model.ClienteProyeccion;
import com.banking.accounts.infrastructure.messaging.ClienteCreatedMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    default ClienteProyeccion toClienteProyeccion(ClienteCreatedMessage message) {
        return ClienteProyeccion.create(message.getClienteId(), message.getNombre());
    }
}
