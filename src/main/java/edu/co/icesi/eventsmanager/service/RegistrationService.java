package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.document.EventRegistration;
import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Transactional(transactionManager = "mongoTransactionManager")
    public void registerToEvent(String eventId, User user) throws Exception {
        if (user == null || user.getId() == null) {
            throw new Exception("Authenticated user is required.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Event not found."));

        // General validations
        if (event.getCapacity() == null || event.getCapacity().getCurrent() == null || event.getCapacity().getMax() == null) {
            throw new Exception("Event capacity configuration is invalid.");
        }

        if (event.getCapacity().getCurrent() >= event.getCapacity().getMax()) {
            throw new Exception("No available spots.");
        }

        if (registrationRepository.existsByEventIdAndUserIdAndStatus(eventId, user.getId(), "ACTIVE")) {
            throw new Exception("You are already registered for this event.");
        }

        // Specific Validations by Type
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        boolean prerequisiteMet = true;
        Double latestSemester = 0.0;

        if (user.getInstitutionRef() != null && "STUDENT".equals(user.getInstitutionRef().getType())) {
            String studentId = user.getInstitutionRef().getId();
            academicValidationService.ensureStudentIsActive(studentId);
            latestSemester = academicValidationService.loadLatestSemester(studentId);

            if ("TALLERES".equals(type) || "TALLER".equals(type)) {
                prerequisiteMet = academicValidationService.validatePrerequisite(studentId, event);
                if (!prerequisiteMet) {
                    throw new Exception("Academic prerequisite not met for this workshop.");
                }
            } else if ("TORNEOS DEPORTIVOS".equals(type) || "TORNEO".equals(type)) {
                if (hasOverlapInSameType(event, user.getId())) {
                    throw new Exception("You have a schedule overlap with another sports tournament.");
                }
            } else if ("ACTIVIDADES DE VOLUNTARIADO".equals(type) || "VOLUNTARIADO".equals(type)) {
                Double minRequiredHours = 0.0; // Prerequisite hours to be able to join
                if (event.getTypeDetails() != null && event.getTypeDetails().getAdditionalProperties() != null) {
                    Object minHours = event.getTypeDetails().getAdditionalProperties().get("requiredHours");
                    if (minHours != null) {
                        try {
                            minRequiredHours = Double.parseDouble(minHours.toString());
                        } catch (NumberFormatException e) {
                            minRequiredHours = 0.0;
                        }
                    }
                }
                
                Double completedHours = user.getAppData() != null ? user.getAppData().getVolunteerHoursCompleted() : 0.0;
                if (completedHours < minRequiredHours) {
                    throw new Exception("Minimum volunteer hours (" + minRequiredHours + ") not met. You have " + completedHours + ".");
                }
            }
        }

        if (hasScheduleConflict(event, user.getId())) {
            throw new Exception("You have a schedule conflict with another active event.");
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
        snapshot.setSemesterAtEnrollment(latestSemester);
        registration.setValidationSnapshot(snapshot);

        registrationRepository.save(registration);

        event.getCapacity().setCurrent(event.getCapacity().getCurrent() + 1);
        eventRepository.save(event);

        statisticsService.refreshEventStatistics(event);
    }

    private boolean hasOverlapInSameType(Event currentEvent, String userId) {
        List<EventRegistration> registrations = registrationRepository.findByUserId(userId);
        for (EventRegistration reg : registrations) {
            if (!"ACTIVE".equals(reg.getStatus())) continue;
            Event other = eventRepository.findById(reg.getEventId()).orElse(null);
            if (other != null && other.getType().equalsIgnoreCase(currentEvent.getType())) {
                if (isOverlapping(currentEvent, other)) return true;
            }
        }
        return false;
    }

    private boolean isOverlapping(Event e1, Event e2) {
        if (e1.getSchedule() == null || e2.getSchedule() == null) return false;
        if (!e1.getSchedule().getDate().equals(e2.getSchedule().getDate())) return false;
        
        LocalTime s1 = LocalTime.parse(e1.getSchedule().getStartTime());
        LocalTime f1 = LocalTime.parse(e1.getSchedule().getEndTime());
        LocalTime s2 = LocalTime.parse(e2.getSchedule().getStartTime());
        LocalTime f2 = LocalTime.parse(e2.getSchedule().getEndTime());
        
        return s1.isBefore(f2) && s2.isBefore(f1);
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

    public List<EventRegistration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public List<EventRegistration> getRegistrationsByEvent(String eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @Transactional(transactionManager = "mongoTransactionManager")
    public void markAttendance(String registrationId, String adminUserId) throws Exception {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new Exception("Registration not found."));

        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new Exception("Event not found."));

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        boolean isAdmin = adminUser != null && adminUser.getRoles() != null && adminUser.getRoles().contains("ADMIN");

        if (!isAdmin && !event.getOrganizerId().equals(adminUserId)) {
            throw new Exception("Not authorized to manage attendance for this event.");
        }

        if (!"ACTIVE".equalsIgnoreCase(registration.getStatus())) {
            throw new Exception("Registration is not in an active state.");
        }

        registration.setStatus("ATTENDED");
        registrationRepository.save(registration);

        // Update volunteer hours if it's a volunteer activity
        if ("ACTIVIDADES DE VOLUNTARIADO".equalsIgnoreCase(event.getType()) || "VOLUNTARIADO".equalsIgnoreCase(event.getType())) {
            User student = userRepository.findById(registration.getUserId()).orElse(null);
            if (student != null) {
                if (student.getAppData() == null) student.setAppData(new User.AppData());
                Double currentHours = student.getAppData().getVolunteerHoursCompleted() != null ? student.getAppData().getVolunteerHoursCompleted() : 0.0;
                
                Double hoursToAdd = 1.0; // Default
                if (event.getTypeDetails() != null && event.getTypeDetails().getAdditionalProperties() != null) {
                    Object hours = event.getTypeDetails().getAdditionalProperties().get("requiredHours");
                    if (hours != null) {
                        try {
                            hoursToAdd = Double.parseDouble(hours.toString());
                        } catch (NumberFormatException e) {
                            // keep default
                        }
                    }
                }
                
                student.getAppData().setVolunteerHoursCompleted(currentHours + hoursToAdd);
                userRepository.save(student);
            }
        }
        
        statisticsService.refreshEventStatistics(event);
    }

    private boolean hasScheduleConflict(Event currentEvent, String userId) {
        if (currentEvent == null || currentEvent.getSchedule() == null) return false;
        
        LocalTime startA = LocalTime.parse(currentEvent.getSchedule().getStartTime());
        LocalTime endA = LocalTime.parse(currentEvent.getSchedule().getEndTime());

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
            LocalTime startB = LocalTime.parse(otherEvent.getSchedule().getStartTime());
            LocalTime endB = LocalTime.parse(otherEvent.getSchedule().getEndTime());
            if (startA.isBefore(endB) && startB.isBefore(endA)) {
                return true;
            }
        }
        return false;
    }
}
