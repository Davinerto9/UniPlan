package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.document.EventRegistration;
import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.entity.EventStatistic;
import edu.co.icesi.eventsmanager.repository.EnrollmentRepository;
import edu.co.icesi.eventsmanager.repository.EventRegistrationRepository;
import edu.co.icesi.eventsmanager.repository.EventRepository;
import edu.co.icesi.eventsmanager.repository.EventStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistrationService {

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EventStatisticRepository statisticRepository;

    public void registerToEvent(String eventId, User user) throws Exception {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Event not found"));

        if (event.getCapacity().getCurrent() >= event.getCapacity().getMax()) {
            throw new Exception("No available spots.");
        }

        if (registrationRepository.existsByEventIdAndUserIdAndStatus(eventId, user.getId(), "ACTIVE")) {
            throw new Exception("You are already registered for this event.");
        }

        String type = event.getType();
        if ("TALLER".equalsIgnoreCase(type)) {
            // Prerequisite logic omitted for brevity
        } else if ("TORNEO_DEPORTIVO".equalsIgnoreCase(type)) {
            // Overlap check
            List<EventRegistration> userRegs = registrationRepository.findByUserId(user.getId());
            for (EventRegistration reg : userRegs) {
                if ("ACTIVE".equals(reg.getStatus())) {
                    Event otherEvent = eventRepository.findById(reg.getEventId()).orElse(null);
                    if (otherEvent != null && "TORNEO_DEPORTIVO".equalsIgnoreCase(otherEvent.getType())) {
                        if (event.getSchedule().getDate().equals(otherEvent.getSchedule().getDate())) {
                            LocalTime start1 = LocalTime.parse(event.getSchedule().getStartTime());
                            LocalTime end1 = LocalTime.parse(event.getSchedule().getEndTime());
                            LocalTime start2 = LocalTime.parse(otherEvent.getSchedule().getStartTime());
                            LocalTime end2 = LocalTime.parse(otherEvent.getSchedule().getEndTime());
                            
                            if (start1.isBefore(end2) && start2.isBefore(end1)) {
                                throw new Exception("Overlap with another sport event.");
                            }
                        }
                    }
                }
            }
        }

        // Register
        EventRegistration reg = new EventRegistration();
        reg.setEventId(eventId);
        reg.setUserId(user.getId());
        reg.setStatus("ACTIVE");
        
        EventRegistration.Timestamps ts = new EventRegistration.Timestamps();
        ts.setEnrolledAt(Instant.now().toString());
        reg.setTimestamps(ts);
        
        registrationRepository.save(reg);

        // Update Capacity
        event.getCapacity().setCurrent(event.getCapacity().getCurrent() + 1);
        eventRepository.save(event);

        updateStatistics(event);
    }

    public void cancelRegistration(String registrationId, User user) throws Exception {
        EventRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new Exception("Registration not found"));
        
        if (!reg.getUserId().equals(user.getId())) {
            throw new Exception("Not authorized");
        }

        reg.setStatus("CANCELLED");
        reg.getTimestamps().setCancelledAt(Instant.now().toString());
        registrationRepository.save(reg);

        Event event = eventRepository.findById(reg.getEventId()).orElse(null);
        if (event != null) {
            event.getCapacity().setCurrent(event.getCapacity().getCurrent() - 1);
            eventRepository.save(event);
            updateStatistics(event);
        }
    }

    private void updateStatistics(Event event) {
        EventStatistic stat = statisticRepository.findById(event.getId()).orElse(new EventStatistic());
        stat.setEventId(event.getId());
        stat.setEventTitle(event.getTitle());
        stat.setEventType(event.getType());
        
        List<EventRegistration> activeRegs = registrationRepository.findByEventIdAndStatus(event.getId(), "ACTIVE");
        List<EventRegistration> cancelledRegs = registrationRepository.findByEventIdAndStatus(event.getId(), "CANCELLED");
        
        stat.setTotalRegistrations(activeRegs.size() + cancelledRegs.size());
        stat.setTotalCancellations(cancelledRegs.size());
        stat.setActualAttendees(activeRegs.size());
        
        double occupancy = (double) activeRegs.size() / event.getCapacity().getMax() * 100;
        stat.setOccupancyPercentage(occupancy);
        
        statisticRepository.save(stat);
    }
}
