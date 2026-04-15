package com.banking.customers.integration;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class CustomerControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockBean
    RabbitTemplate rabbitTemplate;

    @Autowired
    TestRestTemplate testRestTemplate;

    private Map<String, Object> baseRequest(String identificacion) {
        return Map.of(
                "nombre", "Test Cliente",
                "genero", "MASCULINO",
                "edad", 30,
                "identificacion", identificacion,
                "direccion", "Quito Norte",
                "telefono", "+593991234567",
                "contrasena", "Password1",
                "estado", "ACTIVO"
        );
    }

    private Long createCliente(String identificacion) {
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/clientes", baseRequest(identificacion), Map.class);
        return ((Number) response.getBody().get("clienteId")).longValue();
    }

    @Test
    void createClienteWithValidDataShouldReturn201() {
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/clientes", baseRequest("1713175071"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("clienteId");
        assertThat(response.getBody().get("nombre")).isEqualTo("Test Cliente");
    }

    @Test
    void createClienteWithDuplicateIdentificacionShouldReturn409() {
        testRestTemplate.postForEntity("/clientes", baseRequest("1714000005"), Map.class);
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/clientes", baseRequest("1714000005"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createClienteWithInvalidAgeShouldReturn400() {
        Map<String, Object> req = Map.of(
                "nombre", "Menor", "genero", "MASCULINO", "edad", 17,
                "identificacion", "1714000005", "direccion", "Quito", "telefono", "+593991234567",
                "contrasena", "Password1", "estado", "ACTIVO"
        );
        ResponseEntity<Map> response = testRestTemplate.postForEntity("/clientes", req, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getClienteByIdShouldReturn200() {
        Long id = createCliente("1714000013");
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/clientes/" + id, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("clienteId")).isNotNull();
    }

    @Test
    void getClienteWithNonExistentIdShouldReturn404() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/clientes/99999", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateClienteShouldReturn200() {
        Long id = createCliente("1714000021");
        Map<String, Object> updateReq = Map.of("nombre", "Nombre Actualizado");
        ResponseEntity<Map> response = testRestTemplate.exchange(
                "/clientes/" + id, HttpMethod.PUT, new HttpEntity<>(updateReq), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("nombre")).isEqualTo("Nombre Actualizado");
    }

    @Test
    void deleteClienteShouldReturn200AndDeactivate() {
        Long id = createCliente("1714000039");
        ResponseEntity<Map> deleteResponse = testRestTemplate.exchange(
                "/clientes/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody().get("estado")).isEqualTo("INACTIVO");
    }

    @Test
    void deleteNonExistentClienteShouldReturn404() {
        ResponseEntity<Map> response = testRestTemplate.exchange(
                "/clientes/99998", HttpMethod.DELETE, HttpEntity.EMPTY, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void errorResponseShouldHaveStandardFormat() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/clientes/88888", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<?, ?> body = response.getBody();
        assertThat(body.containsKey("timestamp")).isTrue();
        assertThat(body.containsKey("status")).isTrue();
        assertThat(body.containsKey("error")).isTrue();
        assertThat(body.containsKey("message")).isTrue();
        assertThat(body.containsKey("path")).isTrue();
        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("path")).isEqualTo("/clientes/88888");
    }
}
