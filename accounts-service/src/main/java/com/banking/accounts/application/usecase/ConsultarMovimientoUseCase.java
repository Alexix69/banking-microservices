package com.banking.accounts.application.usecase;

import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import org.springframework.stereotype.Service;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.port.MovimientoRepository;

@Service
public class ConsultarMovimientoUseCase {

    private final MovimientoRepository movimientoRepository;

    public ConsultarMovimientoUseCase(MovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    public MovimientoResponse ejecutar(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new MovimientoNotFoundException(id));
        return MovimientoResponse.from(movimiento);
    }
}
