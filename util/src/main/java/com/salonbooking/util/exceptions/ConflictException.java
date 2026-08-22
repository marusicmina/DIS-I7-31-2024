package com.salonbooking.util.exceptions;


public class ConflictException extends RuntimeException {

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }
}
