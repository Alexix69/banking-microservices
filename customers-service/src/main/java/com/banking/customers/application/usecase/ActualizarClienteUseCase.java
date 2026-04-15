package com.banking.customers.application.usecase;

import com.banking.customers.application.dto.ActualizarClienteRequest;
import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.exception.IdentificacionDuplicadaException;
import com.banking.customers.domain.model.Cliente;
import com.banking.customers.domain.model.Genero;
import com.banking.customers.domain.port.ClienteRepository;

public class ActualizarClienteUseCase {

    private final ClienteRepository clienteRepository;

    public ActualizarClienteUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponse ejecutar(Long id, ActualizarClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
        if (request.getIdentificacion() != null &&
                clienteRepository.existsByIdentificacionAndIdNot(request.getIdentificacion(), id)) {
            throw new IdentificacionDuplicadaException(request.getIdentificacion());
        }
        String nombre = request.getNombre() != null ? request.getNombre() : cliente.getNombre();
        Genero genero = request.getGenero() != null ? request.getGenero() : cliente.getGenero();
        int edad = request.getEdad() != null ? request.getEdad() : cliente.getEdad();
        String identificacion = request.getIdentificacion() != null ? request.getIdentificacion() : cliente.getIdentificacion();
        String direccion = request.getDireccion() != null ? request.getDireccion() : cliente.getDireccion();
        String telefono = request.getTelefono() != null ? request.getTelefono() : cliente.getTelefono();
        cliente.actualizarDatos(nombre, genero, edad, identificacion, direccion, telefono);
        return ClienteResponse.from(clienteRepository.save(cliente));
    }
}
