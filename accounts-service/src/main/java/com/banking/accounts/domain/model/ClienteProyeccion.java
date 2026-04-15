package com.banking.accounts.domain.model;

public class ClienteProyeccion {

    private Long clienteId;
    private String nombre;
    private EstadoCliente estado;

    private ClienteProyeccion() {
    }

    public static ClienteProyeccion reconstitute(Long clienteId, String nombre, EstadoCliente estado) {
        ClienteProyeccion p = new ClienteProyeccion();
        p.clienteId = clienteId;
        p.nombre = nombre;
        p.estado = estado;
        return p;
    }

    public static ClienteProyeccion create(Long clienteId, String nombre) {
        ClienteProyeccion p = new ClienteProyeccion();
        p.clienteId = clienteId;
        p.nombre = nombre;
        p.estado = EstadoCliente.ACTIVO;
        return p;
    }

    public boolean estaActivo() {
        return estado == EstadoCliente.ACTIVO;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoCliente getEstado() {
        return estado;
    }
}
