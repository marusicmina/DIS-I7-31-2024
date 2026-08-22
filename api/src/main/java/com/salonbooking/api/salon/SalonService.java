package com.salonbooking.api.salon;

import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface SalonService {

    
    @GetMapping(value = "/salons", produces = "application/json")
    List<Salon> getSalons(@RequestParam(value = "city", required = false) String city);

    
    @GetMapping(value = "/salons/{salonId}", produces = "application/json")
    Salon getSalon(@PathVariable("salonId") long salonId);

    
    @PostMapping(value = "/salons", consumes = "application/json", produces = "application/json")
    Salon createSalon(@RequestBody Salon body);

    
    @PutMapping(value = "/salons/{salonId}", consumes = "application/json", produces = "application/json")
    Salon updateSalon(@PathVariable("salonId") long salonId, @RequestBody Salon body);


   
    @DeleteMapping(value = "/salons/{salonId}")
    void deleteSalon(@PathVariable("salonId") long salonId);

}
