package com.siva.ticketing.controller;

import com.siva.ticketing.model.Seat;
import com.siva.ticketing.service.BookingService;
import com.siva.ticketing.websocket.SeatEventPublisher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;
    private final SeatEventPublisher publisher;

    public BookingController(BookingService bookingService, SeatEventPublisher publisher) {
        this.bookingService = bookingService;
        this.publisher = publisher;
    }

    @PostMapping("/seats/{seatId}/hold")
    public Seat holdSeat(@PathVariable Long seatId) {
        Seat seat = bookingService.holdSeat(seatId);
        publisher.publish(seat);
        return seat;
    }

    @PostMapping("/seats/{seatId}/confirm")
    public Seat confirmSeat(@PathVariable Long seatId) {
        Seat seat = bookingService.confirmSeat(seatId);
        publisher.publish(seat);
        return seat;
    }
}