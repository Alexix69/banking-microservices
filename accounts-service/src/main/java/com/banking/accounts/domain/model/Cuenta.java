package com.banking.accounts.domain.model;

import com.banking.accounts.domain.exception.ClienteInactivoException;
import com.banking.accounts.domain.exception.SaldoInicialInvalidoException;
import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import com.banking.accounts.domain.exception.SaldoMinimoInsuficienteException;

import java.math.BigDecimal;

public class Cuenta {

    private Long id;
    private String numeroCuenta;
    private TipoCuenta tipo;
    private BigDecimal saldoInicial;
    private BigDecimal saldoDisponible;
    private EstadoCuenta estado;
    private Long clienteId;

    private Cuenta() {
    }

    public static Cuenta create(String numeroCuenta, TipoCuenta tipo, BigDecimal saldoInicial,
                                EstadoCuenta estado, ClienteProyeccion clienteProyeccion) {
        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInicialInvalidoException();
        }
        if (tipo == TipoCuenta.CORRIENTE && saldoInicial.compareTo(new BigDecimal("50")) < 0) {
            throw new SaldoMinimoInsuficienteException();
        }
        if (!clienteProyeccion.estaActivo()) {
            throw new ClienteInactivoException();
        }
        Cuenta cuenta = new Cuenta();
        cuenta.numeroCuenta = numeroCuenta;
        cuenta.tipo = tipo;
        cuenta.saldoInicial = saldoInicial;
        cuenta.saldoDisponible = saldoInicial;
        cuenta.estado = estado;
        cuenta.clienteId = clienteProyeccion.getClienteId();
        return cuenta;
    }

    public static Cuenta reconstitute(Long id, String numeroCuenta, TipoCuenta tipo,
                                      BigDecimal saldoInicial, BigDecimal saldoDisponible,
                                      EstadoCuenta estado, Long clienteId) {
        Cuenta cuenta = new Cuenta();
        cuenta.id = id;
        cuenta.numeroCuenta = numeroCuenta;
        cuenta.tipo = tipo;
        cuenta.saldoInicial = saldoInicial;
        cuenta.saldoDisponible = saldoDisponible;
        cuenta.estado = estado;
        cuenta.clienteId = clienteId;
        return cuenta;
    }

    public void aplicarMovimiento(BigDecimal valor) {
        BigDecimal nuevoSaldo = this.saldoDisponible.add(valor);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException();
        }
        this.saldoDisponible = nuevoSaldo;
    }

    public void desactivar() {
        this.estado = EstadoCuenta.INACTIVA;
    }

    public boolean tieneMovimientosRecientes() {
        return false;
    }

    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public Long getClienteId() {
        return clienteId;
    }
}
