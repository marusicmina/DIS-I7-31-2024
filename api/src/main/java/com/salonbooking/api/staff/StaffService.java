package com.salonbooking.api.staff;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API ugovor staff-service-a.
 *
 * checkAvailability je najvazniji endpoint - booking-service ga sinhrono zove
 * pre nego sto potvrdi termin, jer bez odgovora ne sme da nastavi.
 */
public interface StaffService {

    /**
     * Vrati zaposlene, opciono filtrirane po salonu.
     */
    @GetMapping(value = "/staff", produces = "application/json")
    List<Staff> getStaffMembers(@RequestParam(value = "salonId", required = false) Long salonId);

    /**
     * Vrati jednog zaposlenog. Baca NotFoundException (404) ako ne postoji.
     */
    @GetMapping(value = "/staff/{staffId}", produces = "application/json")
    Staff getStaffMember(@PathVariable("staffId") long staffId);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/staff", consumes = "application/json", produces = "application/json")
    Staff createStaffMember(@RequestBody Staff body);

    @PutMapping(value = "/staff/{staffId}", consumes = "application/json", produces = "application/json")
    Staff updateStaffMember(@PathVariable("staffId") long staffId, @RequestBody Staff body);

    @DeleteMapping(value = "/staff/{staffId}")
    void deleteStaffMember(@PathVariable("staffId") long staffId);

    /**
     * Da li zaposleni radi u zadatom intervalu?
     *
     * Vraca available = false ako: zaposleni ne postoji ili nije aktivan, ako tog
     * dana u nedelji ne radi, ili ako trazeni interval izlazi izvan njegovog
     * radnog vremena. Preklapanje sa vec zakazanim terminima NIJE briga ovog
     * servisa - to proverava booking-service u svojoj bazi.
     *
     * Primer poziva:
     * GET /staff/1/availability?start=2026-09-01T10:00:00&end=2026-09-01T10:45:00
     */
    @GetMapping(value = "/staff/{staffId}/availability", produces = "application/json")
    AvailabilityResponse checkAvailability(
            @PathVariable("staffId") long staffId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);
}
