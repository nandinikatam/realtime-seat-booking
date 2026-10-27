package com.siva.ticketing.service;

import com.siva.ticketing.exception.HoldExpiredException;
import com.siva.ticketing.exception.SeatNotFoundException;
import com.siva.ticketing.exception.SeatNotHeldException;
import com.siva.ticketing.exception.SeatUnavailableException;
import com.siva.ticketing.model.Seat;
import com.siva.ticketing.model.SeatStatus;
import com.siva.ticketing.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class BookingService {

    private static final Duration HOLD_DURATION = Duration.ofMinutes(5);

    private final SeatRepository seatRepository;

    public BookingService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Seat holdSeat(Long seatId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));

        Instant now = Instant.now();
        boolean holdExpired = seat.getStatus() == SeatStatus.HELD
                && seat.getHoldExpiresAt() != null
                && seat.getHoldExpiresAt().isBefore(now);

        boolean available = seat.getStatus() == SeatStatus.AVAILABLE || holdExpired;
        if (!available) {
            throw new SeatUnavailableException(seatId);
        }

        seat.setStatus(SeatStatus.HELD);
        seat.setHoldExpiresAt(now.plus(HOLD_DURATION));
        return seat;
    }

    @Transactional
    public Seat confirmSeat(Long seatId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));

        if (seat.getStatus() != SeatStatus.HELD) {
            throw new SeatNotHeldException(seatId);
        }
        if (seat.getHoldExpiresAt() == null || seat.getHoldExpiresAt().isBefore(Instant.now())) {
            throw new HoldExpiredException(seatId);
        }

        seat.setStatus(SeatStatus.BOOKED);
        seat.setHoldExpiresAt(null);
        return seat;
    }

    @Transactional
    public List<Seat> releaseExpiredHolds() {
        List<Seat> expired = seatRepository.findExpiredHolds(SeatStatus.HELD, Instant.now());
        for (Seat seat : expired) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHoldExpiresAt(null);
        }
        return expired;
    }
}