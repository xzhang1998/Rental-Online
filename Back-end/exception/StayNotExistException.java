package com.rental.rentalonline.exception;

public class StayNotExistException extends RuntimeException{
    public StayNotExistException(String message) {
        super(message);
    }
}
