package com.salonbooking.api.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface CatalogService {

    
    @GetMapping(value = "/catalog/services", produces = "application/json")
    List<ServiceOffering> getServices(@RequestParam(value = "salonId", required = false) Long salonId);

    
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
