package com.salonbooking.booking.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /**
     * @LoadBalanced je ono sto omogucava da u kodu pisemo "http://staff-service/..."
     * umesto "http://localhost:8084/...". Spring Cloud presretne zahtev, pita Eureku
     * koje instance nose to ime i izabere jednu (podrazumevano round-robin).
     *
     * Tajmauti su namerno kratki: ako zavisni servis ne odgovori za par sekundi,
     * bolje je odustati nego drzati nit zauzetom. Bez tajmauta bi circuit breaker
     * bio prakticno beskoristan - nikad ne bi stigao da registruje gresku.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
}
