package com.banking.accounts.domain.validator;

import com.banking.accounts.domain.exception.CuentaInactivaException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.EstadoCuenta;

import java.math.BigDecimal;

public class CuentaActivaValidator implements MovimientoValidator {

    @Override
    public void validar(Cuenta cuenta, BigDecimal valor, BigDecimal acumuladoDiario) {
        if (cuenta.getEstado() == EstadoCuenta.INACTIVA) {
            throw new CuentaInactivaException();
        }
    }
}
