package com.siva.ticketing;

import com.siva.ticketing.exception.SeatUnavailableException;
import com.siva.ticketing.model.Seat;
import com.siva.ticketing.model.SeatStatus;
import com.siva.ticketing.repository.SeatRepository;
import com.siva.ticketing.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ConcurrentBookingTest {

    @Autowired
    BookingService bookingService;

    @Autowired
    SeatRepository seatRepository;

    @Test
    void only_one_user_can_hold_the_same_seat() throws Exception {
        Long seatId = 10L;

        // reset the seat so the test can be re-run
        Seat seat = seatRepository.findById(seatId).orElseThrow();
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setHoldExpiresAt(null);
        seatRepository.save(seat);

        int users = 500;
        ExecutorService pool = Executors.newFixedThreadPool(100);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();

        for (int i = 0; i < users; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    bookingService.holdSeat(seatId);
                    success.incrementAndGet();
                } catch (SeatUnavailableException e) {
                    rejected.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }

        start.countDown(); // fire all users at once
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("SUCCESS=" + success.get()
                + " REJECTED=" + rejected.get()
                + " ERRORS=" + errors.get());

        assertEquals(1, success.get(), "exactly one user should get the seat");
        assertEquals(users - 1, rejected.get());
        assertEquals(0, errors.get());
    }
}