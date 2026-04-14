package com.banking.customers.domain.model;

import com.banking.customers.domain.event.DomainEvent;
import com.banking.customers.domain.exception.ContrasenaInvalidaException;
import com.banking.customers.domain.exception.EdadInvalidaException;
import com.banking.customers.domain.exception.IdentificacionInvalidaException;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Persona {

    private String contrasena;
    private EstadoCliente estado;
    private List<DomainEvent> domainEvents = new ArrayList<>();

    private Cliente() {
    }

    private Cliente(Long id, String nombre, Genero genero, int edad,
                    String identificacion, String direccion, String telefono,
                    String contrasena, EstadoCliente estado) {
        super(id, nombre, genero, edad, identificacion, direccion, telefono);
        this.contrasena = contrasena;
        this.estado = estado;
    }

    public static Cliente create(String nombre, Genero genero, int edad,
                                 String identificacion, String direccion, String telefono,
                                 String contrasena, EstadoCliente estadoInicial) {
        validarEdad(edad);
        validarIdentificacion(identificacion);
        validarContrasena(contrasena);
        return new Cliente(null, nombre, genero, edad, identificacion,
                direccion, telefono, contrasena, estadoInicial);
    }

    public static Cliente reconstitute(Long id, String nombre, Genero genero, int edad,
                                       String identificacion, String direccion, String telefono,
                                       String contrasena, EstadoCliente estado) {
        return new Cliente(id, nombre, genero, edad, identificacion,
                direccion, telefono, contrasena, estado);
    }

    public void desactivar() {
        this.estado = EstadoCliente.INACTIVO;
    }

    public void actualizarDatos(String nombre, Genero genero, int edad,
                                String identificacion, String direccion,
                                String telefono) {
        validarEdad(edad);
        validarIdentificacion(identificacion);
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public List<DomainEvent> consumirEventos() {
        List<DomainEvent> copia = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return copia;
    }

    public void registrarEvento(DomainEvent evento) {
        domainEvents.add(evento);
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public String getContrasena() {
        return contrasena;
    }

    private static void validarEdad(int edad) {
        if (edad < 18) {
            throw new EdadInvalidaException();
        }
    }

    private static void validarIdentificacion(String identificacion) {
        if (identificacion == null || !identificacion.matches("\\d{10}")) {
            throw new IdentificacionInvalidaException(identificacion);
        }
        int[] d = identificacion.chars().map(c -> c - '0').toArray();
        int provincia = d[0] * 10 + d[1];
        if (provincia < 1 || provincia > 24 || d[2] >= 6) {
            throw new IdentificacionInvalidaException(identificacion);
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int v = (i % 2 == 0) ? d[i] * 2 : d[i];
            if (v >= 10) v -= 9;
            suma += v;
        }
        if ((10 - suma % 10) % 10 != d[9]) {
            throw new IdentificacionInvalidaException(identificacion);
        }
    }

    private static void validarContrasena(String contrasena) {
        if (contrasena == null || !contrasena.matches("^(?=.*[A-Z])[a-zA-Z0-9]{8,}$")) {
            throw new ContrasenaInvalidaException();
        }
    }
}
