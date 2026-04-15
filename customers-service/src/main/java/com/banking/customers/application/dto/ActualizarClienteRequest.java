package com.banking.customers.application.dto;

import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class ActualizarClienteRequest {

    private final String nombre;

    private final Genero genero;

    @Min(18)
    private final Integer edad;

    private final String identificacion;

    private final String direccion;

    private final String telefono;

    @Size(min = 8)
    private final String contrasena;

    private final EstadoCliente estado;

    public ActualizarClienteRequest(String nombre, Genero genero, Integer edad,
                                    String identificacion, String direccion, String telefono,
                                    String contrasena, EstadoCliente estado) {
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public Genero getGenero() {
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

    public String getContrasena() {
        return contrasena;
    }

    public EstadoCliente getEstado() {
        return estado;
    }
}
