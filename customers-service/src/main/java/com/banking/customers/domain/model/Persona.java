package com.banking.customers.domain.model;

public abstract class Persona {

    protected Long id;
    protected String nombre;
    protected Genero genero;
    protected int edad;
    protected String identificacion;
    protected String direccion;
    protected String telefono;

    protected Persona(Long id, String nombre, Genero genero, int edad,
                      String identificacion, String direccion, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Genero getGenero() {
        return genero;
    }

    public int getEdad() {
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
}
