package com.salonbooking.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service discovery za ceo sistem.
 *
 * Zasto uopste postoji: u mikroservisnoj arhitekturi servisi se pokrecu,
 * gase i skaliraju na vise instanci, pa im se adrese menjaju. Da booking-service
 * u kodu ima zakucano "http://localhost:8084", sistem bi pukao cim se
 * staff-service preseli na drugi port ili se podigne u dve instance.
 *
 * Umesto toga, svaki servis se pri pokretanju prijavi Eureki pod svojim imenom
 * (npr. "staff-service"), a booking-service trazi Eureku po imenu i dobija
 * trenutno vazecu adresu. Ako ima vise instanci, Spring Cloud LoadBalancer
 * sam raspodeljuje pozive medju njima.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
