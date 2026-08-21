package com.salonbooking.util.exceptions;

/**
 * Baca se kada zavisni mikroservis ne odgovara (pad, tajmaut, otvoren circuit breaker).
 * Mapira se na HTTP 503 Service Unavailable - poruka klijentu da nije njegova greska
 * i da pokusa ponovo, za razliku od 4xx gde je problem u samom zahtevu.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException() {
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
