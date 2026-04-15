package com.banking.accounts.domain.validator;

import com.banking.accounts.domain.exception.LimiteDiarioExcedidoException;
import com.banking.accounts.domain.model.Cuenta;

import java.math.BigDecimal;

public class LimiteDiarioValidator implements MovimientoValidator {

    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("500");

    @Override
    public void validar(Cuenta cuenta, BigDecimal valor, BigDecimal acumuladoDiario) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            if (acumuladoDiario.add(valor.abs()).compareTo(LIMITE_DIARIO) > 0) {
                throw new LimiteDiarioExcedidoException();
            }
        }
    }
}
