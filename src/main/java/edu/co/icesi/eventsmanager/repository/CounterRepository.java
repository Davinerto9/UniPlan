package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.document.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepository extends MongoRepository<Counter, String> {
}
