package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
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
    @Column(name = "student_id", length = 15)
    private String studentId;
    
    @Id
    @Column(length = 10)
    private String NRC;
    
    @Column(name = "enrollment_date", nullable = false)
    private Date enrollmentDate;
    
    @Column(length = 15, nullable = false)
    private String status;
    
    @Data
    public static class EnrollmentId implements Serializable {
        private String studentId;
        private String NRC;
    }
}
