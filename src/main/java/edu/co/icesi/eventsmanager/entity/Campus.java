package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CAMPUSES")
public class Campus {
    @Id
    private Integer code;
    
    @Column(length = 20)
    private String name;
    
    @Column(name = "city_code", nullable = false)
    private Integer cityCode;
}
