package com.salonbooking.util.exceptions;


public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
