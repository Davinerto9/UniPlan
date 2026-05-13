package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CITIES")
public class City {
    @Id
    private Integer code;
    
    @Column(length = 20, nullable = false)
    private String name;
    
    @Column(name = "dept_code", nullable = false)
    private Integer deptCode;
}
