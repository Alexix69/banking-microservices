package com.banking.accounts.domain.validator;

import com.banking.accounts.domain.model.Cuenta;

import java.math.BigDecimal;

public interface MovimientoValidator {

    void validar(Cuenta cuenta, BigDecimal valor, BigDecimal acumuladoDiario);
}
