package com.salonbooking.util.exceptions;


public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException() {
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
