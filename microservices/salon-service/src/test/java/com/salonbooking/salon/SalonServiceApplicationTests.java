package com.salonbooking.salon;

import com.salonbooking.api.salon.Salon;
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

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SalonServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("salondb")
            .withUsername("salon")
            .withPassword("salon");

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
    void createAndFetchSalon() {
        Salon newSalon = new Salon(0, "Salon Bella", "Bulevar oslobodjenja 10", "Novi Sad",
                "021123456", "Frizerski i kozmeticki salon", LocalTime.of(9, 0), LocalTime.of(20, 0), 42L, null);

        ResponseEntity<Salon> createResponse = restTemplate.postForEntity(url("/salons"), newSalon, Salon.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Salon created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getSalonId()).isPositive();

        ResponseEntity<Salon> getResponse = restTemplate.getForEntity(url("/salons/" + created.getSalonId()), Salon.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Salon Bella");
    }

    @Test
    void getSalon_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/salons/999999"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getSalon_invalidId_returns422() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/salons/-1"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
