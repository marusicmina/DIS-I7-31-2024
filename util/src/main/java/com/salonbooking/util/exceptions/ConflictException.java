package com.salonbooking.util.exceptions;

/**
 * Baca se npr. kada se pokusa zakazati termin koji se preklapa sa
 * vec postojecim terminom istog zaposlenog (koristi booking-service).
 */
public class ConflictException extends RuntimeException {

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }
}
