package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByStatus(String status);
    List<Event> findByType(String type);
    Optional<Event> findByEventCode(String eventCode);
}
