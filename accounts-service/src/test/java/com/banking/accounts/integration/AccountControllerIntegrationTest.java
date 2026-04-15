package com.banking.accounts.integration;

import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.infrastructure.config.RabbitMQConfig;
import com.banking.accounts.infrastructure.persistence.ClienteProyeccionJpaEntity;
import com.banking.accounts.infrastructure.persistence.CuentaJpaEntity;
import com.banking.accounts.infrastructure.persistence.MovimientoJpaEntity;
import com.banking.accounts.infrastructure.persistence.SpringDataClienteProyeccionRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataCuentaRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataMovimientoRepository;
import com.banking.accounts.domain.model.EstadoCliente;
import com.banking.accounts.domain.model.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class AccountControllerIntegrationTest {

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

    private Long crearClienteActivo(Long clienteId) {
        ClienteProyeccionJpaEntity e = new ClienteProyeccionJpaEntity();
        e.setClienteId(clienteId);
        e.setNombre("Test Cliente");
        e.setEstado(EstadoCliente.ACTIVO);
        clienteProyeccionRepo.save(e);
        return clienteId;
    }

    private Map<String, Object> cuentaRequest(String numero, String tipo, double saldo, Long clienteId) {
        return Map.of(
                "numeroCuenta", numero,
                "tipo", tipo,
                "saldoInicial", saldo,
                "estado", "ACTIVA",
                "clienteId", clienteId
        );
    }

    @Test
    void postCuentasWithActiveClienteShouldReturn201() {
        crearClienteActivo(100L);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-001", "AHORRO", 500.0, 100L), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody().get("numeroCuenta")).isEqualTo("CTA-001");
    }

    @Test
    void postCuentasWithNonexistentClienteShouldReturn422() {
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-002", "AHORRO", 500.0, 999L), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void postCuentasWithDuplicateNumeroCuentaShouldReturn409() {
        crearClienteActivo(101L);
        testRestTemplate.postForEntity("/cuentas", cuentaRequest("CTA-DUP", "AHORRO", 500.0, 101L), Map.class);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-DUP", "AHORRO", 500.0, 101L), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void postCuentasCorrienteWithSaldoLessThan50ShouldReturn400() {
        crearClienteActivo(102L);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-003", "CORRIENTE", 30.0, 102L), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getCuentasByExistingIdShouldReturn200() {
        crearClienteActivo(103L);
        ResponseEntity<Map> created = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-004", "AHORRO", 200.0, 103L), Map.class);
        Long id = ((Number) created.getBody().get("id")).longValue();

        ResponseEntity<Map> response = testRestTemplate.getForEntity("/cuentas/" + id, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getCuentasByNonexistentIdShouldReturn404() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/cuentas/99999", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void putCuentasWithValidUpdateShouldReturn200() {
        crearClienteActivo(104L);
        ResponseEntity<Map> created = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-005", "AHORRO", 200.0, 104L), Map.class);
        Long id = ((Number) created.getBody().get("id")).longValue();

        Map<String, Object> update = Map.of("estado", "INACTIVA");
        ResponseEntity<Map> response = testRestTemplate.exchange(
                "/cuentas/" + id, HttpMethod.PUT, new HttpEntity<>(update), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteCuentaWithNoRecentActivityShouldReturn200() {
        crearClienteActivo(105L);
        ResponseEntity<Map> created = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-006", "AHORRO", 200.0, 105L), Map.class);
        Long id = ((Number) created.getBody().get("id")).longValue();

        ResponseEntity<Map> response = testRestTemplate.exchange(
                "/cuentas/" + id, HttpMethod.DELETE, null, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteCuentaWithActivityInLastYearShouldReturn409() {
        crearClienteActivo(106L);
        ResponseEntity<Map> created = testRestTemplate.postForEntity("/cuentas",
                cuentaRequest("CTA-007", "AHORRO", 200.0, 106L), Map.class);
        Long cuentaId = ((Number) created.getBody().get("id")).longValue();

        CuentaJpaEntity cuentaEntity = cuentaRepo.findById(cuentaId).orElseThrow();
        MovimientoJpaEntity mov = new MovimientoJpaEntity();
        mov.setFecha(LocalDateTime.now().minusDays(10));
        mov.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        mov.setValor(new BigDecimal("100.00"));
        mov.setSaldoResultante(new BigDecimal("300.00"));
        mov.setCuentaId(cuentaEntity.getId());
        movimientoRepo.save(mov);

        ResponseEntity<Map> response = testRestTemplate.exchange(
                "/cuentas/" + cuentaId, HttpMethod.DELETE, null, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
