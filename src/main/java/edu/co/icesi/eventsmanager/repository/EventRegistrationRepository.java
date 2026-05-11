package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.document.EventRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends MongoRepository<EventRegistration, String> {
    List<EventRegistration> findByUserId(String userId);
    List<EventRegistration> findByEventId(String eventId);
    List<EventRegistration> findByEventIdAndStatus(String eventId, String status);
    boolean existsByEventIdAndUserIdAndStatus(String eventId, String userId, String status);
    Optional<EventRegistration> findByEventIdAndUserId(String eventId, String userId);
}
