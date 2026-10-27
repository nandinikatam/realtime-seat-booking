package com.siva.ticketing.controller;

import com.siva.ticketing.model.Event;
import com.siva.ticketing.model.Seat;
import com.siva.ticketing.repository.EventRepository;
import com.siva.ticketing.repository.SeatRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventController(EventRepository eventRepository, SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/events")
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/events/{eventId}/seats")
    public List<Seat> getSeats(@PathVariable Long eventId) {
        return seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId);
    }
}