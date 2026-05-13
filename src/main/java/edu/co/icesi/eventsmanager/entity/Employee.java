package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Column;
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
    @Column(length = 15)
    private String id;

    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;

    @Column(length = 30, nullable = false)
    private String email;

    @Column(name = "contract_type", length = 30, nullable = false)
    private String contractType;

    @Column(name = "employee_type", length = 30, nullable = false)
    private String employeeType;

    @Column(name = "faculty_code", nullable = false)
    private Integer facultyCode;

    @Column(name = "campus_code", nullable = false)
    private Integer campusCode;

    @Column(name = "birth_place_code", nullable = false)
    private Integer birthPlaceCode;
}
