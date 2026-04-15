package com.banking.accounts.infrastructure.persistence;

import com.banking.accounts.domain.model.EstadoCliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente_proyeccion")
public class ClienteProyeccionJpaEntity {

    @Id
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private EstadoCliente estado;

    public ClienteProyeccionJpaEntity() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }
}
