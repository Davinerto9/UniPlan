package edu.co.icesi.eventsmanager.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Organizers")
public class Organizer {
    @Id
    private String id;
    private String userId;
    private String organizerType;
    private TypeDetails typeDetails;
    private Boolean isActive;

    @Data
    public static class TypeDetails {
        private Double facultyCode;
        private Double departmentCode;
        private String areaCode;
    }
}
