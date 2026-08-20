package com.salonbooking.catalog;

import com.salonbooking.api.catalog.ServiceOffering;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("catalogdb")
            .withUsername("catalog")
            .withPassword("catalog");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void createAndFetchService() {
        ServiceOffering newService = new ServiceOffering(0, 1L, "Zensko sisanje",
                "Pranje, sisanje i fen", 45, new BigDecimal("1500.00"), true, null);

        ResponseEntity<ServiceOffering> createResponse =
                restTemplate.postForEntity(url("/catalog/services"), newService, ServiceOffering.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ServiceOffering created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getServiceId()).isPositive();
        assertThat(created.getDurationMinutes()).isEqualTo(45);

        ResponseEntity<ServiceOffering> getResponse =
                restTemplate.getForEntity(url("/catalog/services/" + created.getServiceId()), ServiceOffering.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Zensko sisanje");
    }

    @Test
    void getServices_filteredBySalon() {
        ServiceOffering forSalon99 = new ServiceOffering(0, 99L, "Masaza lica",
                "Relaks tretman", 60, new BigDecimal("2000.00"), true, null);
        restTemplate.postForEntity(url("/catalog/services"), forSalon99, ServiceOffering.class);

        ResponseEntity<ServiceOffering[]> response =
                restTemplate.getForEntity(url("/catalog/services?salonId=99"), ServiceOffering[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allMatch(s -> s.getSalonId() == 99L);
    }

    @Test
    void getService_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/catalog/services/999999"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createService_zeroDuration_returns422() {
        ServiceOffering invalid = new ServiceOffering(0, 1L, "Neispravna usluga",
                null, 0, new BigDecimal("500.00"), true, null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url("/catalog/services"), invalid, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
