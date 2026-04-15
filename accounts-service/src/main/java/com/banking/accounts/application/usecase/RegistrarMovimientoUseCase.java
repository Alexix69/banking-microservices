package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.CrearMovimientoRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import org.springframework.stereotype.Service;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.model.Cuenta;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import com.banking.accounts.domain.validator.MovimientoValidator;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RegistrarMovimientoUseCase {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final List<MovimientoValidator> validators;

    public RegistrarMovimientoUseCase(CuentaRepository cuentaRepository,
                                      MovimientoRepository movimientoRepository,
                                      List<MovimientoValidator> validators) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.validators = validators;
    }

    public MovimientoResponse ejecutar(CrearMovimientoRequest request) {
        Cuenta cuenta = cuentaRepository.findById(request.getCuentaId())
                .orElseThrow(() -> new CuentaNotFoundException(request.getCuentaId()));
        BigDecimal acumuladoDiario = movimientoRepository
                .sumRetirosDiariosByClienteId(cuenta.getClienteId());
        BigDecimal valor = request.getValor();
        validators.forEach(v -> v.validar(cuenta, valor, acumuladoDiario));
        BigDecimal saldoResultante = cuenta.getSaldoDisponible().add(valor);
        Movimiento movimiento = Movimiento.create(resolverTipo(valor), valor,
                saldoResultante, cuenta.getId(), null, null);
        cuenta.aplicarMovimiento(valor);
        Movimiento guardado = movimientoRepository.save(movimiento);
        cuentaRepository.save(cuenta);
        return MovimientoResponse.from(guardado);
    }

    private TipoMovimiento resolverTipo(BigDecimal valor) {
        return valor.compareTo(BigDecimal.ZERO) > 0 ? TipoMovimiento.DEPOSITO : TipoMovimiento.RETIRO;
    }
}
