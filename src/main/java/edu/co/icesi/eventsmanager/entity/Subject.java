package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "SUBJECTS")
public class Subject {
    @Id
    private String code;
    private String name;
    private Integer programCode;
}
