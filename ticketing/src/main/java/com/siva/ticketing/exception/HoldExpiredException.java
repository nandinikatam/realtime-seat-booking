package com.siva.ticketing.exception;

public class HoldExpiredException extends RuntimeException {
    public HoldExpiredException(Long seatId) {
        super("Your hold on seat " + seatId + " has expired");
    }
}