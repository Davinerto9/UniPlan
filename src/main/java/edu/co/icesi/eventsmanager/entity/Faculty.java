package edu.co.icesi.eventsmanager.entity;

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
    private String name;
    private String location;
    private String phoneNumber;
    private String deanId;
}
