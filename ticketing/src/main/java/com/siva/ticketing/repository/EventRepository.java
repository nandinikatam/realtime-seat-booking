package com.siva.ticketing.repository;

import com.siva.ticketing.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}