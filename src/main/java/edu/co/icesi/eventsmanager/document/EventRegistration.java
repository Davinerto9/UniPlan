package edu.co.icesi.eventsmanager.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Events_registrations")
public class EventRegistration {
    @Id
    private String id;
    private String eventId;
    private String userId;
    private String status;
    private Timestamps timestamps;
    private ValidationSnapshot validationSnapshot;

    @Data
    public static class Timestamps {
        private String enrolledAt;
        private String cancelledAt;
    }

    @Data
    public static class ValidationSnapshot {
        private Double volunteerHoursAtEnrollment;
        private Boolean prerequisiteMet;
        private Double semesterAtEnrollment;
    }
}
