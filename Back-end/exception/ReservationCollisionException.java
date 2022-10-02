package com.rental.rentalonline.exception;

public class ReservationCollisionException extends RuntimeException {
    public ReservationCollisionException(String message) {
        super(message);
    }
}
