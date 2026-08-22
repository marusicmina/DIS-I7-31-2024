package com.salonbooking.api.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface BookingService {

   
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/bookings", consumes = "application/json", produces = "application/json")
    Booking createBooking(@RequestBody CreateBookingRequest body);

    
    @GetMapping(value = "/bookings", produces = "application/json")
    List<Booking> getBookings(@RequestParam(value = "clientId", required = false) Long clientId,
                               @RequestParam(value = "staffId", required = false) Long staffId,
                               @RequestParam(value = "salonId", required = false) Long salonId);

    @GetMapping(value = "/bookings/{bookingId}", produces = "application/json")
    Booking getBooking(@PathVariable("bookingId") long bookingId);

    
    @PostMapping(value = "/bookings/{bookingId}/cancel", produces = "application/json")
    Booking cancelBooking(@PathVariable("bookingId") long bookingId);

    
    @PostMapping(value = "/bookings/{bookingId}/complete", produces = "application/json")
    Booking completeBooking(@PathVariable("bookingId") long bookingId);
}
