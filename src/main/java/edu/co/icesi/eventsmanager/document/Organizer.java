package edu.co.icesi.eventsmanager.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Data
@Document(collection = "Organizers")
public class Organizer {
    @Id
    private String id;
    private String userId;
    private String organizerType; // PROFESSOR, LEADER, WELLBEING
    private TypeDetails typeDetails;
    private Boolean isActive;

    @Data
    public static class TypeDetails {
        // Professor specific
        private Double facultyCode;
        private Double departmentCode;
        private String areaCode;
        
        // Student Leader specific
        private Integer academicProgramCode;
        private Integer semester;
        private String groupRepresented;
        
        // Wellbeing specific
        private String administrativeArea;
        private String position;
        
        // Flexible for future needs
        private Map<String, Object> additionalAttributes;
    }
}
