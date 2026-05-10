package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.document.Organizer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizerRepository extends MongoRepository<Organizer, String> {
}
