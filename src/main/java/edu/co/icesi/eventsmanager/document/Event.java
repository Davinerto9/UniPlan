package edu.co.icesi.eventsmanager.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "Events")
public class Event {
    @Id
    private String id;
    private String title;
    private String description;
    private String type;
    private String status;
    private Schedule schedule;
    private Location location;
    private Capacity capacity;
    private String organizerId;
    private TypeDetails typeDetails;
    private String createdAt;

    @Data
    public static class Schedule {
        private String date;
        private String startTime;
        private String endTime;
    }

    @Data
    public static class Location {
        private String name;
        private Double campusCode;
    }

    @Data
    public static class Capacity {
        private Double max;
        private Double current;
    }

    @Data
    public static class TypeDetails {
        private List<String> materials;
        private Prerequisite prerequisite;
    }

    @Data
    public static class Prerequisite {
        private String type;
        private String subjectCode;
    }
}
