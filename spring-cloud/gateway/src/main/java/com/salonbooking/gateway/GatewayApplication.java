package com.salonbooking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Jedina javna ulazna tacka sistema.
 *
 * Bez gateway-a klijent bi morao da zna sest adresa i portova (8081 za salone,
 * 8083 za usluge...) i da se nosi sa CORS-om prema svakom od njih posebno.
 * Sa gateway-em postoji jedna adresa - localhost:8080 - a on prosledjuje
 * zahteve odgovarajucem servisu, koji pronalazi preko Eureke.
 *
 * Ovde je i prirodno mesto za sve sto vazi za ceo sistem: provera JWT tokena,
 * ogranicavanje broja zahteva, centralno logovanje saobracaja.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
