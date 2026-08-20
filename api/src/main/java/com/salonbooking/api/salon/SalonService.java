package com.salonbooking.api.salon;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API ugovor salon-service-a. Implementira ga salon-service, a konzumiraju ga
 * booking-service (sinhroni REST poziv radi provere da li salon postoji) i gateway.
 *
 * Napomena: ovaj interfejs namerno koristi samo standardne Spring MVC anotacije
 * (bez OpenAPI/Swagger anotacija), kako api modul ne bi povlacio dodatne
 * zavisnosti (springdoc) na runtime classpath svih mikroservisa koji ga koriste.
 * Svaki mikroservis moze nezavisno da ukljuci springdoc-openapi u svom build.gradle
 * ako zeli Swagger UI (salon-service to vec radi).
 */
public interface SalonService {

    /**
     * Vrati sve salone, opciono filtrirane po gradu.
     */
    @GetMapping(value = "/salons", produces = "application/json")
    List<Salon> getSalons(@RequestParam(value = "city", required = false) String city);

    /**
     * Vrati detalje jednog salona po ID-u. Baca NotFoundException (404) ako ne postoji.
     */
    @GetMapping(value = "/salons/{salonId}", produces = "application/json")
    Salon getSalon(@PathVariable long salonId);

    /**
     * Kreiraj novi salon.
     */
    @PostMapping(value = "/salons", consumes = "application/json", produces = "application/json")
    Salon createSalon(@RequestBody Salon body);

    /**
     * Izmeni postojeci salon.
     */
    @PutMapping(value = "/salons/{salonId}", consumes = "application/json", produces = "application/json")
    Salon updateSalon(@PathVariable long salonId, @RequestBody Salon body);

    /**
     * Obrisi salon.
     */
    @DeleteMapping(value = "/salons/{salonId}")
    void deleteSalon(@PathVariable long salonId);
}
