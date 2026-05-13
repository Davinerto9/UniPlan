package edu.co.icesi.eventsmanager.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "Users")
public class User {
    @Id
    private String id;
    private Auth auth;
    private InstitutionRef institutionRef;
    private List<String> roles;
    private Boolean isActive;
    private String createdAt;
    private AppData appData;

    @Data
    public static class Auth {
        private String email;
        private String passwordHash;
    }

    @Data
    public static class InstitutionRef {
        private String type; // "STUDENT" or "EMPLOYEE"
        private String id; // Student/Employee code
    }

    @Data
    public static class AppData {
        private Double volunteerHoursCompleted = 0.0;
    }
}
