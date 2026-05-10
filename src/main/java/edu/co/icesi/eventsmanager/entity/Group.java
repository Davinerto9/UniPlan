package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "GROUPS")
public class Group {
    @Id
    private String NRC;
    private Integer number;
    private String semester;
    private String subjectCode;
    private String professorId;
}
