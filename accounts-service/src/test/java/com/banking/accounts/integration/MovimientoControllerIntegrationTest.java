package com.banking.accounts.integration;

import com.banking.accounts.domain.model.EstadoCliente;
import com.banking.accounts.domain.model.EstadoCuenta;
import com.banking.accounts.domain.model.TipoCuenta;
import com.banking.accounts.infrastructure.persistence.ClienteProyeccionJpaEntity;
import com.banking.accounts.infrastructure.persistence.CuentaJpaEntity;
import com.banking.accounts.infrastructure.persistence.MovimientoJpaEntity;
import com.banking.accounts.infrastructure.persistence.SpringDataClienteProyeccionRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataCuentaRepository;
import com.banking.accounts.infrastructure.persistence.SpringDataMovimientoRepository;
import com.banking.accounts.domain.model.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.time.LocalDate;
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
class MovimientoControllerIntegrationTest {

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

    private Long setupClienteYCuenta(Long clienteId, String numeroCuenta, double saldo) {
        ClienteProyeccionJpaEntity cliente = new ClienteProyeccionJpaEntity();
        cliente.setClienteId(clienteId);
        cliente.setNombre("Test Cliente");
        cliente.setEstado(EstadoCliente.ACTIVO);
        clienteProyeccionRepo.save(cliente);

        CuentaJpaEntity cuenta = new CuentaJpaEntity();
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setTipo(TipoCuenta.AHORRO);
        cuenta.setSaldoInicial(new BigDecimal(String.valueOf(saldo)));
        cuenta.setSaldoDisponible(new BigDecimal(String.valueOf(saldo)));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setClienteId(clienteId);
        return cuentaRepo.save(cuenta).getId();
    }

    @Test
    void postMovimientosWithPositiveValueShouldReturn201AsDeposito() {
        Long cuentaId = setupClienteYCuenta(200L, "MOV-001", 1000.0);
        Map<String, Object> request = Map.of("cuentaId", cuentaId, "valor", 500.0);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/movimientos", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("tipoMovimiento")).isEqualTo("DEPOSITO");
    }

    @Test
    void postMovimientosRetiroWithInsufficientSaldoShouldReturn422WithExactMessage() {
        Long cuentaId = setupClienteYCuenta(201L, "MOV-002", 100.0);
        Map<String, Object> request = Map.of("cuentaId", cuentaId, "valor", -200.0);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/movimientos", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("message")).isEqualTo("Saldo no disponible");
    }

    @Test
    void postMovimientosWithDailyLimitExceededShouldReturn422WithExactMessage() {
        Long cuentaId = setupClienteYCuenta(202L, "MOV-003", 2000.0);

        Map<String, Object> retiro1 = Map.of("cuentaId", cuentaId, "valor", -400.0);
        testRestTemplate.postForEntity("/movimientos", retiro1, Map.class);

        Map<String, Object> retiro2 = Map.of("cuentaId", cuentaId, "valor", -200.0);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/movimientos", retiro2, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("message")).isEqualTo("Límite de retiro diario excedido");
    }

    @Test
    void postMovimientosWithZeroValueShouldReturn400WithExactMessage() {
        Long cuentaId = setupClienteYCuenta(203L, "MOV-004", 1000.0);
        Map<String, Object> request = Map.of("cuentaId", cuentaId, "valor", 0.0);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/movimientos", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("El valor del movimiento no puede ser cero");
    }

    @Test
    void postAjustesWithoutJustificacionShouldReturn400() {
        Long cuentaId = setupClienteYCuenta(204L, "MOV-005", 1000.0);
        MovimientoJpaEntity mov = crearMovimiento(cuentaId, TipoMovimiento.DEPOSITO, new BigDecimal("200"));
        Map<String, Object> request = Map.of(
                "movimientoOrigenId", mov.getId(),
                "valor", 50.0,
                "justificacion", ""
        );
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/ajustes", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postAjustesWithJustificacionShouldReturn201() {
        Long cuentaId = setupClienteYCuenta(205L, "MOV-006", 1000.0);
        MovimientoJpaEntity mov = crearMovimiento(cuentaId, TipoMovimiento.DEPOSITO, new BigDecimal("200"));
        Map<String, Object> request = Map.of(
                "movimientoOrigenId", mov.getId(),
                "valor", 50.0,
                "justificacion", "Corrección de monto"
        );
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/ajustes", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("tipoMovimiento")).isEqualTo("AJUSTE");
    }

    @Test
    void postReversionesWithValidOrigenShouldReturn201() {
        Long cuentaId = setupClienteYCuenta(206L, "MOV-007", 1000.0);
        MovimientoJpaEntity mov = crearMovimiento(cuentaId, TipoMovimiento.DEPOSITO, new BigDecimal("300"));
        Map<String, Object> request = Map.of("movimientoOrigenId", mov.getId());
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/reversiones", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("tipoMovimiento")).isEqualTo("REVERSION");
    }

    @Test
    void getMovimientosByExistingIdShouldReturn200() {
        Long cuentaId = setupClienteYCuenta(207L, "MOV-008", 1000.0);
        MovimientoJpaEntity mov = crearMovimiento(cuentaId, TipoMovimiento.DEPOSITO, new BigDecimal("100"));
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/movimientos/" + mov.getId(), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getMovimientosByNonexistentIdShouldReturn404() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/movimientos/99999", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getReportesWithValidParametersShouldReturn200() {
        Long cuentaId = setupClienteYCuenta(208L, "MOV-009", 1000.0);
        crearMovimiento(cuentaId, TipoMovimiento.DEPOSITO, new BigDecimal("100"));
        String url = "/reportes?clienteId=208&fechaInicio=" + LocalDate.now().minusDays(1) + "&fechaFin=" + LocalDate.now();
        ResponseEntity<Object> response = testRestTemplate.getForEntity(url, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getReportesWithoutParametersShouldReturn400() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/reportes", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getReportesWithNonexistentClienteShouldReturn404() {
        String url = "/reportes?clienteId=99999&fechaInicio=" + LocalDate.now().minusDays(1) + "&fechaFin=" + LocalDate.now();
        ResponseEntity<Map> response = testRestTemplate.getForEntity(url, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private MovimientoJpaEntity crearMovimiento(Long cuentaId, TipoMovimiento tipo, BigDecimal valor) {
        MovimientoJpaEntity mov = new MovimientoJpaEntity();
        mov.setFecha(LocalDateTime.now());
        mov.setTipoMovimiento(tipo);
        mov.setValor(tipo == TipoMovimiento.RETIRO ? valor.negate() : valor);
        mov.setSaldoResultante(new BigDecimal("1000.00").add(valor));
        mov.setCuentaId(cuentaId);
        return movimientoRepo.save(mov);
    }
}
