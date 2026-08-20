package com.salonbooking.api.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API ugovor catalog-service-a.
 *
 * Glavni potrosac je booking-service: pre nego sto potvrdi termin, sinhrono
 * poziva getService(serviceId) da sazna trajanje i cenu usluge i da proveri
 * da usluga uopste postoji i da je aktivna.
 */
public interface CatalogService {

    /**
     * Vrati usluge, opciono filtrirane po salonu.
     */
    @GetMapping(value = "/catalog/services", produces = "application/json")
    List<ServiceOffering> getServices(@RequestParam(value = "salonId", required = false) Long salonId);

    /**
     * Vrati jednu uslugu. Baca NotFoundException (404) ako ne postoji.
     */
    @GetMapping(value = "/catalog/services/{serviceId}", produces = "application/json")
    ServiceOffering getService(@PathVariable("serviceId") long serviceId);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/catalog/services", consumes = "application/json", produces = "application/json")
    ServiceOffering createService(@RequestBody ServiceOffering body);

    @PutMapping(value = "/catalog/services/{serviceId}", consumes = "application/json", produces = "application/json")
    ServiceOffering updateService(@PathVariable("serviceId") long serviceId, @RequestBody ServiceOffering body);

    @DeleteMapping(value = "/catalog/services/{serviceId}")
    void deleteService(@PathVariable("serviceId") long serviceId);
}
