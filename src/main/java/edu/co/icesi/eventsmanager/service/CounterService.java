package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Counter;
import edu.co.icesi.eventsmanager.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CounterService {

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private MongoOperations mongoOperations;

    private static final String EVENT_COUNTER_ID = "event_id";

    public Long getNextEventId() {
        Counter counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(EVENT_COUNTER_ID)),
                new Update().inc("sequence", 1),
                Counter.class
        );

        if (counter == null) {
            // Si no existe, crea el contador inicial
            counter = new Counter(EVENT_COUNTER_ID, 1L);
            mongoOperations.save(counter);
            return 1L;
        }

        return counter.getSequence() + 1;
    }

    public void initializeEventCounter() {
        Optional<Counter> existing = counterRepository.findById(EVENT_COUNTER_ID);
        if (existing.isEmpty()) {
            Counter counter = new Counter(EVENT_COUNTER_ID, 0L);
            counterRepository.save(counter);
        }
    }
}
