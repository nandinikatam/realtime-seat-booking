package com.siva.ticketing.exception;

public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(Long seatId) {
        super("Seat " + seatId + " was just taken by another user");
    }
}