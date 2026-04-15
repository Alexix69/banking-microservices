package com.banking.accounts.unit.domain;

import com.banking.accounts.domain.exception.BusinessRuleException;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.exception.DomainException;
import com.banking.accounts.domain.exception.DuplicateResourceException;
import com.banking.accounts.domain.exception.LimiteDiarioExcedidoException;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.exception.NumeroCuentaDuplicadoException;
import com.banking.accounts.domain.exception.ResourceNotFoundException;
import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHierarchyTest {

    @Test
    void domainExceptionShouldBeRuntimeException() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException();
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void businessRuleExceptionShouldExtendDomainException() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException();
        assertInstanceOf(DomainException.class, ex);
        assertInstanceOf(BusinessRuleException.class, ex);
    }

    @Test
    void saldoInsuficienteExceptionShouldHaveExactMessage() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException();
        assertEquals("Saldo no disponible", ex.getMessage());
    }

    @Test
    void limiteDiarioExcedidoExceptionShouldHaveExactMessage() {
        LimiteDiarioExcedidoException ex = new LimiteDiarioExcedidoException();
        assertEquals("Límite de retiro diario excedido", ex.getMessage());
    }

    @Test
    void cuentaNotFoundExceptionShouldIncludeIdInMessage() {
        CuentaNotFoundException ex = new CuentaNotFoundException(42L);
        assertInstanceOf(ResourceNotFoundException.class, ex);
        assertInstanceOf(DomainException.class, ex);
        assertTrue(ex.getMessage().contains("42"));
    }

    @Test
    void movimientoNotFoundExceptionShouldExtendResourceNotFoundException() {
        MovimientoNotFoundException ex = new MovimientoNotFoundException(10L);
        assertInstanceOf(ResourceNotFoundException.class, ex);
        assertTrue(ex.getMessage().contains("10"));
    }

    @Test
    void numeroCuentaDuplicadoExceptionShouldExtendDuplicateResourceException() {
        NumeroCuentaDuplicadoException ex = new NumeroCuentaDuplicadoException("ACC-001");
        assertInstanceOf(DuplicateResourceException.class, ex);
        assertTrue(ex.getMessage().contains("ACC-001"));
    }
}
