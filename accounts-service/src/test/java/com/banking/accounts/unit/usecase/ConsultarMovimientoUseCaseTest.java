package com.banking.accounts.unit.usecase;

import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.application.usecase.ConsultarMovimientoUseCase;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.model.Movimiento;
import com.banking.accounts.domain.model.TipoMovimiento;
import com.banking.accounts.domain.port.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultarMovimientoUseCaseTest {

    private MovimientoRepository movimientoRepository;
    private ConsultarMovimientoUseCase useCase;

    @BeforeEach
    void setUp() {
        movimientoRepository = mock(MovimientoRepository.class);
        useCase = new ConsultarMovimientoUseCase(movimientoRepository);
    }

    @Test
    void existingMovimientoShouldReturnResponse() {
        Movimiento movimiento = Movimiento.reconstitute(5L, LocalDateTime.now(),
                TipoMovimiento.DEPOSITO, new BigDecimal("100.00"),
                new BigDecimal("300.00"), 1L, null, null);
        when(movimientoRepository.findById(5L)).thenReturn(Optional.of(movimiento));

        MovimientoResponse response = useCase.ejecutar(5L);

        assertEquals(5L, response.getId());
        assertEquals(TipoMovimiento.DEPOSITO, response.getTipoMovimiento());
    }

    @Test
    void nonExistentMovimientoShouldThrowNotFoundException() {
        when(movimientoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MovimientoNotFoundException.class, () -> useCase.ejecutar(99L));
    }
}
