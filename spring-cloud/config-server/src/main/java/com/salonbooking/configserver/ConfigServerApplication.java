package com.salonbooking.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Centralizovana konfiguracija za ceo sistem.
 *
 * Problem koji resava: sest servisa je do sada imalo isti blok podesavanja za
 * actuator i logovanje, prepisan u svakom application.yml. Promena jedne
 * vrednosti znacila je izmenu na sest mesta i sest novih build-ova. Sa config
 * serverom to stoji na jednom mestu (/config-repo), a servisi ga povuku pri
 * pokretanju.
 *
 * Koristi se "native" rezim - konfiguracija se cita iz foldera. U pravoj
 * produkciji bi se citala iz Git repozitorijuma, cime se dobija istorija
 * promena konfiguracije i mogucnost vracanja na prethodnu verziju.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
