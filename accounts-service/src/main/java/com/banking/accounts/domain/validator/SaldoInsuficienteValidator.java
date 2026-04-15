package com.banking.accounts.domain.validator;

import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import com.banking.accounts.domain.model.Cuenta;

import java.math.BigDecimal;

public class SaldoInsuficienteValidator implements MovimientoValidator {

    @Override
    public void validar(Cuenta cuenta, BigDecimal valor, BigDecimal acumuladoDiario) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            if (cuenta.getSaldoDisponible().compareTo(valor.abs()) < 0) {
                throw new SaldoInsuficienteException();
            }
        }
    }
}
