package com.salonbooking.auth;

import com.salonbooking.api.auth.*;
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

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("authdb")
            .withUsername("auth")
            .withPassword("auth");

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
    void registerLoginAndFetchUser_fullFlow() {
        RegisterRequest register = new RegisterRequest("Mina", "Marusic", "mina.test@example.com",
                "sigurnaLozinka123", Role.CUSTOMER);

        ResponseEntity<UserSummary> registerResponse = restTemplate.postForEntity(url("/auth/register"), register, UserSummary.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserSummary created = registerResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getUserId()).isPositive();
        assertThat(created.getEmail()).isEqualTo("mina.test@example.com");

        // Duplikat email -> 409 Conflict
        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(url("/auth/register"), register, String.class);
        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Uspesan login -> dobijamo JWT token
        LoginRequest login = new LoginRequest("mina.test@example.com", "sigurnaLozinka123");
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(url("/auth/login"), login, AuthResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getToken()).isNotBlank();

        // Pogresna lozinka -> 401 Unauthorized
        LoginRequest wrongLogin = new LoginRequest("mina.test@example.com", "pogresnaLozinka");
        ResponseEntity<String> wrongLoginResponse = restTemplate.postForEntity(url("/auth/login"), wrongLogin, String.class);
        assertThat(wrongLoginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // GET korisnika ne sme da sadrzi lozinku (proveravamo da body ne pominje "password"/"Hash")
        ResponseEntity<String> getUserResponse = restTemplate.getForEntity(url("/auth/users/" + created.getUserId()), String.class);
        assertThat(getUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getUserResponse.getBody()).doesNotContainIgnoringCase("password");
        assertThat(getUserResponse.getBody()).doesNotContain("$2a$");
    }

    @Test
    void getUser_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/auth/users/999999"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void register_shortPassword_returns422() {
        RegisterRequest invalid = new RegisterRequest("Ana", "Anic", "ana@example.com", "123", Role.CUSTOMER);
        ResponseEntity<String> response = restTemplate.postForEntity(url("/auth/register"), invalid, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
