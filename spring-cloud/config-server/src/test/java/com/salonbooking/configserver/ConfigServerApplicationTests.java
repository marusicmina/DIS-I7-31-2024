package com.salonbooking.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false")
class ConfigServerApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void servesSharedConfigurationForAnyService() {
        // /{ime-servisa}/{profil} je standardni Config Server endpoint.
        ResponseEntity<String> response =
                restTemplate.getForEntity(url("/salon-service/default"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Vrednost dolazi iz config-repo/application.yml
        assertThat(response.getBody()).contains("com.salonbooking");
    }

    @Test
    void servesServiceSpecificConfigurationForBooking() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(url("/booking-service/default"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Vrednost dolazi iz config-repo/booking-service.yml
        assertThat(response.getBody()).contains("resilience4j");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
