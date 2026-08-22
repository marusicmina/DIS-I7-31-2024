package com.salonbooking.api.staff;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

public interface StaffService {

    
    @GetMapping(value = "/staff", produces = "application/json")
    List<Staff> getStaffMembers(@RequestParam(value = "salonId", required = false) Long salonId);

    
    @GetMapping(value = "/staff/{staffId}", produces = "application/json")
    Staff getStaffMember(@PathVariable("staffId") long staffId);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/staff", consumes = "application/json", produces = "application/json")
    Staff createStaffMember(@RequestBody Staff body);

    @PutMapping(value = "/staff/{staffId}", consumes = "application/json", produces = "application/json")
    Staff updateStaffMember(@PathVariable("staffId") long staffId, @RequestBody Staff body);

    @DeleteMapping(value = "/staff/{staffId}")
    void deleteStaffMember(@PathVariable("staffId") long staffId);

    
    @GetMapping(value = "/staff/{staffId}/availability", produces = "application/json")
    AvailabilityResponse checkAvailability(
            @PathVariable("staffId") long staffId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);
}
