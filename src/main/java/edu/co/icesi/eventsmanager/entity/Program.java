package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "PROGRAMS")
public class Program {
    @Id
    private Integer code;
    
    @Column(length = 40, nullable = false)
    private String name;
    
    @Column(name = "area_code", nullable = false)
    private Integer areaCode;
}
