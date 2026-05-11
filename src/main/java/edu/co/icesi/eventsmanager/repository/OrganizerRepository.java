package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.document.Organizer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OrganizerRepository extends MongoRepository<Organizer, String> {
    Optional<Organizer> findByUserId(String userId);
    List<Organizer> findByOrganizerType(String organizerType);
    List<Organizer> findByIsActive(Boolean isActive);
}
