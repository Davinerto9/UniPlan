package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "SUBJECTS")
public class Subject {
    @Id
    @Column(length = 10)
    private String code;
    
    @Column(length = 30, nullable = false)
    private String name;
    
    @Column(name = "program_code", nullable = false)
    private Integer programCode;
}
