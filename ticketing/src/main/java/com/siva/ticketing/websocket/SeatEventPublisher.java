package com.siva.ticketing.websocket;

import com.siva.ticketing.model.Seat;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeatEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public SeatEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(Seat seat) {
        messagingTemplate.convertAndSend("/topic/events/" + seat.getEventId() + "/seats", seat);
    }

    public void publishAll(List<Seat> seats) {
        seats.forEach(this::publish);
    }
}