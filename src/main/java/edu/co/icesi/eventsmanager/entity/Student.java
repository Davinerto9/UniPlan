package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "STUDENTS")
public class Student {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Date birthDate;
    private Integer birthPlaceCode;
    private Integer campusCode;
    @Column(name = "is_active")
    private Boolean isActive;
    private String academicStatus;
}
