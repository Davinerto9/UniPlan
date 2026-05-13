package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "AREAS")
public class Area {
    @Id
    private Integer code;
    
    @Column(length = 20, nullable = false)
    private String name;
    
    @Column(name = "faculty_code", nullable = false)
    private Integer facultyCode;
    
    @Column(name = "coordinator_id", length = 15, nullable = false)
    private String coordinatorId;
}
