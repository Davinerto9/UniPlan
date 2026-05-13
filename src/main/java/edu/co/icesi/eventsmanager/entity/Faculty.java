package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "FACULTIES")
public class Faculty {
    @Id
    private Integer code;
    
    @Column(length = 40, nullable = false)
    private String name;
    
    @Column(length = 15, nullable = false)
    private String location;
    
    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;
    
    @Column(name = "dean_id", length = 15)
    private String deanId;
}
