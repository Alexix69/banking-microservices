package com.banking.accounts.integration;

import com.banking.accounts.domain.model.EstadoCliente;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.infrastructure.config.RabbitMQConfig;
import com.banking.accounts.infrastructure.persistence.ClienteProyeccionJpaEntity;
import com.banking.accounts.infrastructure.persistence.CuentaJpaEntity;
import com.banking.accounts.infrastructure.persistence.MovimientoJpaEntity;
import com.banking.accounts.infrastructure.persistence.SpringDataClienteProyeccionRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataCuentaRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataMovimientoRepository;
import com.banking.accounts.domain.model.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class ClienteEventConsumerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    SpringDataClienteProyeccionRepository clienteProyeccionRepo;

    @Autowired
    SpringDataCuentaRepository cuentaRepo;

    @Autowired
    SpringDataMovimientoRepository movimientoRepo;

    @BeforeEach
    void cleanUp() {
        movimientoRepo.deleteAll();
        cuentaRepo.deleteAll();
        clienteProyeccionRepo.deleteAll();
    }

    @Test
    void clienteCreatedEventShouldInsertProyeccion() {
        Map<String, Object> event = Map.of(
                "clienteId", 10L,
                "nombre", "Jose Lema",
                "estado", "ACTIVO"
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CLIENTE,
                RabbitMQConfig.ROUTING_CREATED,
                event
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var proyeccion = clienteProyeccionRepo.findByClienteId(10L);
            assertThat(proyeccion).isPresent();
            assertThat(proyeccion.get().getNombre()).isEqualTo("Jose Lema");
            assertThat(proyeccion.get().getEstado()).isEqualTo(EstadoCliente.ACTIVO);
        });
    }

    @Test
    void clienteCreatedEventReceivedTwiceShouldBeIdempotent() {
        Map<String, Object> event = Map.of(
                "clienteId", 11L,
                "nombre", "Jose Lema",
                "estado", "ACTIVO"
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_CLIENTE, RabbitMQConfig.ROUTING_CREATED, event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_CLIENTE, RabbitMQConfig.ROUTING_CREATED, event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = clienteProyeccionRepo.findAll().stream()
                    .filter(p -> p.getClienteId().equals(11L))
                    .count();
            assertThat(count).isEqualTo(1);
        });
    }

    @Test
    void clienteDesactivadoEventShouldUpdateProyeccionEstado() {
        ClienteProyeccionJpaEntity proyeccion = buildProyeccion(20L, "Mariana", EstadoCliente.ACTIVO);
        clienteProyeccionRepo.save(proyeccion);

        Map<String, Object> event = Map.of("clienteId", 20L);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CLIENTE,
                RabbitMQConfig.ROUTING_DESACTIVADO,
                event
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var updated = clienteProyeccionRepo.findByClienteId(20L);
            assertThat(updated).isPresent();
            assertThat(updated.get().getEstado()).isEqualTo(EstadoCliente.INACTIVO);
        });
    }

    @Test
    void clienteDesactivadoEventShouldDeactivateAllActiveCuentasWithoutValidatingAge() {
        ClienteProyeccionJpaEntity proyeccion = buildProyeccion(30L, "Carlos", EstadoCliente.ACTIVO);
        clienteProyeccionRepo.save(proyeccion);

        CuentaJpaEntity cuenta1 = buildCuenta("CTA-001", 30L, EstadoCuenta.ACTIVA);
        CuentaJpaEntity cuenta2 = buildCuenta("CTA-002", 30L, EstadoCuenta.ACTIVA);
        cuentaRepo.save(cuenta1);
        cuentaRepo.save(cuenta2);

        Map<String, Object> event = Map.of("clienteId", 30L);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CLIENTE,
                RabbitMQConfig.ROUTING_DESACTIVADO,
                event
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var cuentas = cuentaRepo.findByClienteId(30L);
            assertThat(cuentas).hasSize(2);
            assertThat(cuentas).allMatch(c -> c.getEstado() == EstadoCuenta.INACTIVA);
        });
    }

    @Test
    void clienteDesactivadoEventWithRecentMovimientosCuentaShouldAlsoDeactivate() {
        ClienteProyeccionJpaEntity proyeccion = buildProyeccion(40L, "Luis", EstadoCliente.ACTIVO);
        clienteProyeccionRepo.save(proyeccion);

        CuentaJpaEntity cuenta = buildCuenta("CTA-003", 40L, EstadoCuenta.ACTIVA);
        CuentaJpaEntity savedCuenta = cuentaRepo.save(cuenta);

        MovimientoJpaEntity mov = new MovimientoJpaEntity();
        mov.setFecha(LocalDateTime.now().minusDays(10));
        mov.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        mov.setValor(new BigDecimal("500.00"));
        mov.setSaldoResultante(new BigDecimal("1500.00"));
        mov.setCuentaId(savedCuenta.getId());
        movimientoRepo.save(mov);

        Map<String, Object> event = Map.of("clienteId", 40L);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CLIENTE,
                RabbitMQConfig.ROUTING_DESACTIVADO,
                event
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var cuentas = cuentaRepo.findByClienteId(40L);
            assertThat(cuentas).hasSize(1);
            assertThat(cuentas.get(0).getEstado()).isEqualTo(EstadoCuenta.INACTIVA);
        });
    }

    @Test
    void fullFlowCreateClientePublishEventAndCreateCuenta() {
        Long clienteId = 50L;
        Map<String, Object> event = Map.of(
                "clienteId", clienteId,
                "nombre", "Ana Gomez",
                "estado", "ACTIVO"
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CLIENTE,
                RabbitMQConfig.ROUTING_CREATED,
                event
        );

        await().atMost(5, TimeUnit.SECONDS).until(() ->
                clienteProyeccionRepo.findByClienteId(clienteId).isPresent()
        );

        Map<String, Object> cuentaRequest = Map.of(
                "numeroCuenta", "CTA-E2E-001",
                "tipo", "AHORRO",
                "saldoInicial", 750.00,
                "estado", "ACTIVA",
                "clienteId", clienteId
        );

        ResponseEntity<Map> response = testRestTemplate.postForEntity("/cuentas", cuentaRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number saldoDisponible = (Number) response.getBody().get("saldoDisponible");
        Number saldoInicial = (Number) response.getBody().get("saldoInicial");
        assertThat(saldoDisponible.doubleValue()).isEqualTo(saldoInicial.doubleValue());
    }

    @Test
    void flujoCompletoDesactivarClientePublicarEventoYVerificarCuentasInactivas() {
        Long clienteId = 60L;
        ClienteProyeccionJpaEntity proyeccion = buildProyeccion(clienteId, "Pedro Torres", EstadoCliente.ACTIVO);
        clienteProyeccionRepo.save(proyeccion);

        CuentaJpaEntity cuenta1 = buildCuenta("CTA-E2E-002", clienteId, EstadoCuenta.ACTIVA);
        CuentaJpaEntity cuenta2 = buildCuenta("CTA-E2E-003", clienteId, EstadoCuenta.ACTIVA);
        CuentaJpaEntity savedCuenta1 = cuentaRepo.save(cuenta1);
        cuentaRepo.save(cuenta2);

        MovimientoJpaEntity mov = new MovimientoJpaEntity();
        mov.setFecha(LocalDateTime.now().minusDays(10));
        mov.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        mov.setValor(new BigDecimal("200.00"));
        mov.setSaldoResultante(new BigDecimal("1200.00"));
        mov.setCuentaId(savedCuenta1.getId());
        movimientoRepo.save(mov);

        Map<String, Object> event = Map.of("clienteId", clienteId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CLIENTE,
                RabbitMQConfig.ROUTING_DESACTIVADO,
                event
        );

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var updated = clienteProyeccionRepo.findByClienteId(clienteId);
            assertThat(updated).isPresent();
            assertThat(updated.get().getEstado()).isEqualTo(EstadoCliente.INACTIVO);
        });

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var cuentas = cuentaRepo.findByClienteId(clienteId);
            assertThat(cuentas).hasSize(2);
            assertThat(cuentas).allMatch(c -> c.getEstado() == EstadoCuenta.INACTIVA);
        });

        Map<String, Object> nuevaCuentaRequest = Map.of(
                "numeroCuenta", "CTA-E2E-004",
                "tipo", "AHORRO",
                "saldoInicial", 500.00,
                "estado", "ACTIVA",
                "clienteId", clienteId
        );
        ResponseEntity<Map> createResponse = testRestTemplate.postForEntity("/cuentas", nuevaCuentaRequest, Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ClienteProyeccionJpaEntity buildProyeccion(Long clienteId, String nombre, EstadoCliente estado) {
        ClienteProyeccionJpaEntity e = new ClienteProyeccionJpaEntity();
        e.setClienteId(clienteId);
        e.setNombre(nombre);
        e.setEstado(estado);
        return e;
    }

    private CuentaJpaEntity buildCuenta(String numero, Long clienteId, EstadoCuenta estado) {
        CuentaJpaEntity e = new CuentaJpaEntity();
        e.setNumeroCuenta(numero);
        e.setTipo(TipoCuenta.AHORRO);
        e.setSaldoInicial(new BigDecimal("1000.00"));
        e.setSaldoDisponible(new BigDecimal("1000.00"));
        e.setEstado(estado);
        e.setClienteId(clienteId);
        return e;
    }
}
