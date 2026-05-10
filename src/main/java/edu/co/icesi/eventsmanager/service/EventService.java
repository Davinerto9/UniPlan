package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByType(String type) {
        return eventRepository.findByType(type);
    }

    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }
}
