package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "GROUPS")
public class Group {
    @Id
    @Column(length = 10)
    private String NRC;
    
    @Column(nullable = false)
    private Integer number;
    
    @Column(length = 6, nullable = false)
    private String semester;
    
    @Column(name = "subject_code", length = 10, nullable = false)
    private String subjectCode;
    
    @Column(name = "professor_id", length = 15, nullable = false)
    private String professorId;
}
