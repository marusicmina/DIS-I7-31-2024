package com.salonbooking.salon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = "com.salonbooking")
@EnableDiscoveryClient
public class SalonServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(SalonServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SalonServiceApplication.class, args);

        Environment env = ctx.getEnvironment();
        String postgresHost = env.getProperty("spring.datasource.url");
        LOG.info("salon-service pokrenut, konektovan na bazu: {}", postgresHost);
    }
}
