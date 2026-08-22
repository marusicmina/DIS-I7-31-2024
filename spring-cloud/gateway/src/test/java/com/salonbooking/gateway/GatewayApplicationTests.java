package com.salonbooking.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "eureka.client.enabled=false")
class GatewayApplicationTests {

    @Autowired
    private RouteDefinitionLocator routeLocator;

    @Test
    void contextLoads() {
    }

    @Test
    void allExpectedRoutesAreConfigured() {
        List<String> routeIds = routeLocator.getRouteDefinitions()
                .map(r -> r.getId())
                .collectList()
                .block();

        assertThat(routeIds).contains(
                "auth-service", "salon-service", "catalog-service",
                "staff-service", "booking-service");
    }

    @Test
    void routesUseLoadBalancedUris() {
        List<String> uris = routeLocator.getRouteDefinitions()
                .map(r -> r.getUri().toString())
                .collectList()
                .block();

        assertThat(uris).isNotEmpty();
        assertThat(uris).allMatch(u -> u.startsWith("lb://"));
    }
}
