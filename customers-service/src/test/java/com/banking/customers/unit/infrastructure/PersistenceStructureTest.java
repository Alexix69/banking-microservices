package com.banking.customers.unit.infrastructure;

import com.banking.customers.domain.port.ClienteRepository;
import com.banking.customers.infrastructure.persistence.CustomerRepositoryJpa;
import com.banking.customers.infrastructure.persistence.SpringDataClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceStructureTest {

    @Test
    void customerRepositoryJpaShouldImplementClienteRepository() {
        assertTrue(ClienteRepository.class.isAssignableFrom(CustomerRepositoryJpa.class));
    }

    @Test
    void springDataRepositoryShouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(SpringDataClienteRepository.class));
    }
}
