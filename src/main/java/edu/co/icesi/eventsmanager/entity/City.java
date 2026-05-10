package edu.co.icesi.eventsmanager.entity;

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
    private String name;
    private Integer deptCode;
}
