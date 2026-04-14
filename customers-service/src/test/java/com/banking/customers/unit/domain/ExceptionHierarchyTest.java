package com.banking.customers.unit.domain;

import com.banking.customers.domain.exception.BusinessRuleException;
import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.exception.ContrasenaInvalidaException;
import com.banking.customers.domain.exception.DomainException;
import com.banking.customers.domain.exception.DuplicateResourceException;
import com.banking.customers.domain.exception.EdadInvalidaException;
import com.banking.customers.domain.exception.IdentificacionDuplicadaException;
import com.banking.customers.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHierarchyTest {

    @Test
    void domainExceptionShouldBeRuntimeException() {
        EdadInvalidaException ex = new EdadInvalidaException();
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void businessRuleExceptionShouldExtendDomainException() {
        ContrasenaInvalidaException ex = new ContrasenaInvalidaException();
        assertInstanceOf(DomainException.class, ex);
        assertInstanceOf(BusinessRuleException.class, ex);
    }

    @Test
    void resourceNotFoundExceptionShouldExtendDomainException() {
        ClienteNotFoundException ex = new ClienteNotFoundException(1L);
        assertInstanceOf(DomainException.class, ex);
        assertInstanceOf(ResourceNotFoundException.class, ex);
    }

    @Test
    void duplicateResourceExceptionShouldExtendDomainException() {
        IdentificacionDuplicadaException ex = new IdentificacionDuplicadaException("1713175071");
        assertInstanceOf(DomainException.class, ex);
        assertInstanceOf(DuplicateResourceException.class, ex);
    }

    @Test
    void clienteNotFoundExceptionShouldExtendResourceNotFoundException() {
        ClienteNotFoundException ex = new ClienteNotFoundException(42L);
        assertInstanceOf(ResourceNotFoundException.class, ex);
    }

    @Test
    void identificicacionDuplicadaExceptionShouldExtendDuplicateResourceException() {
        IdentificacionDuplicadaException ex = new IdentificacionDuplicadaException("1713175071");
        assertInstanceOf(DuplicateResourceException.class, ex);
    }

    @Test
    void edadInvalidaExceptionShouldHaveCorrectMessage() {
        EdadInvalidaException ex = new EdadInvalidaException();
        assertEquals("La edad debe ser mayor o igual a 18 años", ex.getMessage());
    }

    @Test
    void clienteNotFoundExceptionShouldIncludeIdInMessage() {
        ClienteNotFoundException ex = new ClienteNotFoundException(99L);
        assertTrue(ex.getMessage().contains("99"));
    }
}
