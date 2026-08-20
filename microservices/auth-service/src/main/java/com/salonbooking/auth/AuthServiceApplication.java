package com.salonbooking.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

// scanBasePackages = "com.salonbooking": pokriva com.salonbooking.auth.* (ovaj servis) i
// com.salonbooking.util.* (GlobalControllerExceptionHandler, JwtUtil) - isti razlog kao u salon-service.
@SpringBootApplication(scanBasePackages = "com.salonbooking")
@EnableDiscoveryClient
public class AuthServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(AuthServiceApplication.class, args);
        LOG.info("auth-service pokrenut na portu: {}", ctx.getEnvironment().getProperty("server.port"));
    }
}
