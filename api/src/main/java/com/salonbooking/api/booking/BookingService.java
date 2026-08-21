package com.salonbooking.api.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API ugovor booking-service-a - orkestratora zakazivanja.
 */
public interface BookingService {

    /**
     * Zakazi termin. Pre upisa se sinhrono proverava:
     * salon postoji, usluga postoji i pripada tom salonu, zaposleni tada radi,
     * i da nema preklapanja sa vec zakazanim terminom istog zaposlenog.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/bookings", consumes = "application/json", produces = "application/json")
    Booking createBooking(@RequestBody CreateBookingRequest body);

    /**
     * Termini, opciono filtrirani po klijentu, zaposlenom ili salonu.
     */
    @GetMapping(value = "/bookings", produces = "application/json")
    List<Booking> getBookings(@RequestParam(value = "clientId", required = false) Long clientId,
                               @RequestParam(value = "staffId", required = false) Long staffId,
                               @RequestParam(value = "salonId", required = false) Long salonId);

    @GetMapping(value = "/bookings/{bookingId}", produces = "application/json")
    Booking getBooking(@PathVariable("bookingId") long bookingId);

    /**
     * Otkazi termin. Oslobadja slot - preko otkazanog termina moze da se zakaze novi.
     */
    @PostMapping(value = "/bookings/{bookingId}/cancel", produces = "application/json")
    Booking cancelBooking(@PathVariable("bookingId") long bookingId);

    /**
     * Oznaci termin kao odrzan. Tek posle ovoga klijent sme da ostavi recenziju.
     */
    @PostMapping(value = "/bookings/{bookingId}/complete", produces = "application/json")
    Booking completeBooking(@PathVariable("bookingId") long bookingId);
}
