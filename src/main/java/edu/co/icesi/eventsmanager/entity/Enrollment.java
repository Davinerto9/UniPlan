package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "ENROLLMENTS")
@IdClass(Enrollment.EnrollmentId.class)
public class Enrollment {
    @Id
    private String studentId;
    @Id
    private String NRC;
    
    private Date enrollmentDate;
    private String status;
    
    @Data
    public static class EnrollmentId implements Serializable {
        private String studentId;
        private String NRC;
    }
}
