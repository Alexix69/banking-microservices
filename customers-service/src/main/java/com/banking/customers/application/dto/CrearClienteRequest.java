package com.banking.customers.application.dto;

import com.banking.customers.domain.model.EstadoCliente;
import com.banking.customers.domain.model.Genero;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearClienteRequest {

    @NotBlank
    private final String nombre;

    @NotNull
    private final Genero genero;

    @NotNull
    @Min(18)
    private final Integer edad;

    @NotBlank
    private final String identificacion;

    @NotBlank
    private final String direccion;

    @NotBlank
    private final String telefono;

    @NotBlank
    @Size(min = 8)
    private final String contrasena;

    @NotNull
    private final EstadoCliente estado;

    public CrearClienteRequest(String nombre, Genero genero, Integer edad,
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
