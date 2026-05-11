package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public Event saveEvent(Event event) {
        validateEvent(event);

        if (event.getCreatedAt() == null) {
            event.setCreatedAt(Instant.now().toString());
        }

        if (event.getEventCode() == null || event.getEventCode().isBlank()) {
            event.setEventCode(generateEventCode());
        }

        if (event.getCapacity() != null && event.getCapacity().getCurrent() == null) {
            event.getCapacity().setCurrent(0.0);
        }

        if (event.getStatus() == null) {
            event.setStatus("PUBLISHED");
        }

        if (event.getType() != null) {
            event.setType(event.getType().toUpperCase());
        }

        return eventRepository.save(event);
    }

    public void deleteEvent(String eventId) {
        eventRepository.deleteById(eventId);
    }

    public List<Event> getEventsByOrganizer(String organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    private void validateEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (event.getSchedule() == null || event.getSchedule().getDate() == null) {
            throw new IllegalArgumentException("Event schedule must be defined.");
        }

        try {
            LocalDate eventDate = LocalDate.parse(event.getSchedule().getDate());
            if (!eventDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Event date must be in the future.");
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Event schedule date is invalid.");
        }

        if (event.getCapacity() == null || event.getCapacity().getMax() == null) {
            throw new IllegalArgumentException("Event capacity must be defined.");
        }

        if (event.getCapacity().getMax() <= 0) {
            throw new IllegalArgumentException("Event capacity must be greater than zero.");
        }
    }

    private String generateEventCode() {
        return "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
