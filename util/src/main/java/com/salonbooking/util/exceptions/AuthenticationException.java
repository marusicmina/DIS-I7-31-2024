package com.salonbooking.util.exceptions;

/**
 * Baca se kada login podaci nisu ispravni (pogresan email/lozinka) ili je
 * token nevalidan/istekao. Mapira se na HTTP 401 Unauthorized.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
