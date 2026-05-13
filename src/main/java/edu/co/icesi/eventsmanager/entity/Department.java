package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "DEPARTMENTS")
public class Department {
    @Id
    private Integer code;
    
    @Column(length = 20, nullable = false)
    private String name;
    
    @Column(name = "country_code", nullable = false)
    private Integer countryCode;
}
