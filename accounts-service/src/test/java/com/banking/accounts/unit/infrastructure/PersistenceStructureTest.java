package com.banking.accounts.unit.infrastructure;

import com.banking.accounts.domain.port.ClienteProyeccionRepository;
import com.banking.accounts.domain.port.CuentaRepository;
import com.banking.accounts.domain.port.MovimientoRepository;
import com.banking.accounts.infrastructure.persistence.AccountRepositoryJpa;
import com.banking.accounts.infrastructure.persistence.ClienteProyeccionJpaEntity;
import com.banking.accounts.infrastructure.persistence.ClienteProyeccionRepositoryJpa;
import com.banking.accounts.infrastructure.persistence.CuentaJpaEntity;
import com.banking.accounts.infrastructure.persistence.MovimientoJpaEntity;
import com.banking.accounts.infrastructure.persistence.MovimientoRepositoryJpa;
import com.banking.accounts.infrastructure.persistence.SpringDataClienteProyeccionRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataCuentaRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataMovimientoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceStructureTest {

    @Test
    void springDataCuentaRepositoryShouldExtendJpaRepository() {
        assertThat(JpaRepository.class).isAssignableFrom(SpringDataCuentaRepository.class);
        assertThat(SpringDataCuentaRepository.class.getGenericInterfaces())
                .anyMatch(t -> t.getTypeName().contains("CuentaJpaEntity"));
    }

    @Test
    void accountRepositoryJpaShouldImplementCuentaRepository() {
        assertThat(CuentaRepository.class).isAssignableFrom(AccountRepositoryJpa.class);
    }

    @Test
    void movimientoRepositoryJpaShouldImplementMovimientoRepository() {
        assertThat(MovimientoRepository.class).isAssignableFrom(MovimientoRepositoryJpa.class);
    }

    @Test
    void clienteProyeccionRepositoryJpaShouldImplementPort() {
        assertThat(ClienteProyeccionRepository.class).isAssignableFrom(ClienteProyeccionRepositoryJpa.class);
    }

    @Test
    void springDataMovimientoRepositoryShouldExtendJpaRepository() {
        assertThat(JpaRepository.class).isAssignableFrom(SpringDataMovimientoRepository.class);
        assertThat(SpringDataMovimientoRepository.class.getGenericInterfaces())
                .anyMatch(t -> t.getTypeName().contains("MovimientoJpaEntity"));
    }

    @Test
    void springDataClienteProyeccionRepositoryShouldExtendJpaRepository() {
        assertThat(JpaRepository.class).isAssignableFrom(SpringDataClienteProyeccionRepository.class);
        assertThat(SpringDataClienteProyeccionRepository.class.getGenericInterfaces())
                .anyMatch(t -> t.getTypeName().contains("ClienteProyeccionJpaEntity"));
    }
}
