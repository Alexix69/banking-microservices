package com.banking.accounts.domain.validator;

import com.banking.accounts.domain.exception.ValorMovimientoInvalidoException;
import com.banking.accounts.domain.model.Cuenta;

import java.math.BigDecimal;

public class ValorCeroValidator implements MovimientoValidator {

    @Override
    public void validar(Cuenta cuenta, BigDecimal valor, BigDecimal acumuladoDiario) {
        if (valor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValorMovimientoInvalidoException();
        }
    }
}
