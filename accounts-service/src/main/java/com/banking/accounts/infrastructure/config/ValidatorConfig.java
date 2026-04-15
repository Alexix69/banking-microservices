package com.banking.accounts.infrastructure.config;

import com.banking.accounts.domain.validator.CuentaActivaValidator;
import com.banking.accounts.domain.validator.LimiteDiarioValidator;
import com.banking.accounts.domain.validator.MovimientoValidator;
import com.banking.accounts.domain.validator.SaldoInsuficienteValidator;
import com.banking.accounts.domain.validator.ValorCeroValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ValidatorConfig {

    @Bean
    public List<MovimientoValidator> movimientoValidators() {
        return List.of(
                new ValorCeroValidator(),
                new CuentaActivaValidator(),
                new SaldoInsuficienteValidator(),
                new LimiteDiarioValidator()
        );
    }
}
