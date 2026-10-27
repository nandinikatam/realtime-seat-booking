package com.siva.ticketing.scheduler;

import com.siva.ticketing.model.Seat;
import com.siva.ticketing.service.BookingService;
import com.siva.ticketing.websocket.SeatEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HoldExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(HoldExpiryJob.class);

    private final BookingService bookingService;
    private final SeatEventPublisher publisher;

    public HoldExpiryJob(BookingService bookingService, SeatEventPublisher publisher) {
        this.bookingService = bookingService;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 10_000)
    public void releaseExpiredHolds() {
        List<Seat> released = bookingService.releaseExpiredHolds();
        if (!released.isEmpty()) {
            log.info("Released {} expired seat holds", released.size());
            publisher.publishAll(released);
        }
    }
}