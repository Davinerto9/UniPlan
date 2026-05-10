package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "EMPLOYEES")
public class Employee {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String contractType;
    private String employeeType;
    private Integer facultyCode;
    private Integer campusCode;
    private Integer birthPlaceCode;
}
