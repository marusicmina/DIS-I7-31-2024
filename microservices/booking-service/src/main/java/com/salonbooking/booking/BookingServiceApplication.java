package com.salonbooking.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.salonbooking")
@EnableDiscoveryClient
public class BookingServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(BookingServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(BookingServiceApplication.class, args);
        LOG.info("booking-service pokrenut, konektovan na bazu: {}",
                ctx.getEnvironment().getProperty("spring.datasource.url"));
    }
}
