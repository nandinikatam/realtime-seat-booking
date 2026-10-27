package com.siva.ticketing.exception;

public class SeatNotHeldException extends RuntimeException {
    public SeatNotHeldException(Long seatId) {
        super("Seat " + seatId + " is not currently held");
    }
}