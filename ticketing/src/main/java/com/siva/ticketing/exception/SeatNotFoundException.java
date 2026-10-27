package com.siva.ticketing.exception;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(Long seatId) {
        super("Seat " + seatId + " not found");
    }
}