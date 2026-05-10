package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.document.EventRegistration;
import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.service.AcademicValidationService;
import edu.co.icesi.eventsmanager.service.StatisticsService;
import edu.co.icesi.eventsmanager.repository.EventRegistrationRepository;
import edu.co.icesi.eventsmanager.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private AcademicValidationService academicValidationService;

    @Autowired
    private StatisticsService statisticsService;

    @Transactional(transactionManager = "mongoTransactionManager")
    public void registerToEvent(String eventId, User user) throws Exception {
        if (user == null || user.getId() == null) {
            throw new Exception("Authenticated user is required.");
        }

        academicValidationService.ensureStudentIsActive(user.getInstitutionRef().getId());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Event not found."));

        if (event.getCapacity() == null || event.getCapacity().getCurrent() == null || event.getCapacity().getMax() == null) {
            throw new Exception("Event capacity configuration is invalid.");
        }

        if (event.getCapacity().getCurrent() >= event.getCapacity().getMax()) {
            throw new Exception("No available spots.");
        }

        if (registrationRepository.existsByEventIdAndUserIdAndStatus(eventId, user.getId(), "ACTIVE")) {
            throw new Exception("You are already registered for this event.");
        }

        if (hasScheduleConflict(event, user.getId())) {
            throw new Exception("You have a schedule conflict with another active event.");
        }

        boolean prerequisiteMet = academicValidationService.validatePrerequisite(user.getInstitutionRef().getId(), event);
        if (!prerequisiteMet) {
            throw new Exception("Academic prerequisite not met for this event.");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(user.getId());
        registration.setStatus("ACTIVE");
        registration.setCreatedAt(Instant.now().toString());

        EventRegistration.Timestamps timestamps = new EventRegistration.Timestamps();
        timestamps.setEnrolledAt(Instant.now().toString());
        registration.setTimestamps(timestamps);

        EventRegistration.ValidationSnapshot snapshot = new EventRegistration.ValidationSnapshot();
        snapshot.setVolunteerHoursAtEnrollment(user.getAppData() != null ? user.getAppData().getVolunteerHoursCompleted() : 0.0);
        snapshot.setPrerequisiteMet(prerequisiteMet);
        snapshot.setSemesterAtEnrollment(academicValidationService.loadLatestSemester(user.getInstitutionRef().getId()));
        registration.setValidationSnapshot(snapshot);

        registrationRepository.save(registration);

        event.getCapacity().setCurrent(event.getCapacity().getCurrent() + 1);
        eventRepository.save(event);

        statisticsService.refreshEventStatistics(event);
    }

    @Transactional(transactionManager = "mongoTransactionManager")
    public void cancelRegistration(String registrationId, User user) throws Exception {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new Exception("Registration not found."));

        if (!registration.getUserId().equals(user.getId())) {
            throw new Exception("Not authorized to cancel this registration.");
        }

        if ("CANCELLED".equalsIgnoreCase(registration.getStatus())) {
            throw new Exception("Registration is already cancelled.");
        }

        registration.setStatus("CANCELLED");
        if (registration.getTimestamps() == null) {
            registration.setTimestamps(new EventRegistration.Timestamps());
        }
        registration.getTimestamps().setCancelledAt(Instant.now().toString());
        registrationRepository.save(registration);

        Event event = eventRepository.findById(registration.getEventId()).orElse(null);
        if (event != null && event.getCapacity() != null && event.getCapacity().getCurrent() != null) {
            double updatedCurrent = event.getCapacity().getCurrent() - 1;
            event.getCapacity().setCurrent(Math.max(updatedCurrent, 0));
            eventRepository.save(event);
            statisticsService.refreshEventStatistics(event);
        }
    }

    private boolean hasScheduleConflict(Event currentEvent, String userId) {
        List<EventRegistration> registrations = registrationRepository.findByUserId(userId);
        for (EventRegistration registration : registrations) {
            if (!"ACTIVE".equalsIgnoreCase(registration.getStatus())) {
                continue;
            }
            Event otherEvent = eventRepository.findById(registration.getEventId()).orElse(null);
            if (otherEvent == null || otherEvent.getSchedule() == null || currentEvent.getSchedule() == null) {
                continue;
            }
            if (!currentEvent.getSchedule().getDate().equals(otherEvent.getSchedule().getDate())) {
                continue;
            }
            LocalTime startA = LocalTime.parse(currentEvent.getSchedule().getStartTime());
            LocalTime endA = LocalTime.parse(currentEvent.getSchedule().getEndTime());
            LocalTime startB = LocalTime.parse(otherEvent.getSchedule().getStartTime());
            LocalTime endB = LocalTime.parse(otherEvent.getSchedule().getEndTime());
            if (startA.isBefore(endB) && startB.isBefore(endA)) {
                return true;
            }
        }
        return false;
    }
}
