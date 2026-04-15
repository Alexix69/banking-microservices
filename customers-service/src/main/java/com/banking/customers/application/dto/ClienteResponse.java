package com.banking.customers.application.dto;

import com.banking.customers.domain.model.Cliente;

public class ClienteResponse {

    private final Long clienteId;
    private final String nombre;
    private final String genero;
    private final Integer edad;
    private final String identificacion;
    private final String direccion;
    private final String telefono;
    private final String estado;

    public ClienteResponse(Long clienteId, String nombre, String genero, Integer edad,
                           String identificacion, String direccion, String telefono,
                           String estado) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.telefono = telefono;
        this.estado = estado;
    }

    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getGenero().name(),
                cliente.getEdad(),
                cliente.getIdentificacion(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEstado().name()
        );
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEstado() {
        return estado;
    }
}
