package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.entity.Student;
import edu.co.icesi.eventsmanager.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AcademicValidationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StudentRepository studentRepository;

    public void ensureStudentIsActive(String studentId) throws Exception {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new Exception("Student not found in institutional database.");
        }
    }

    public boolean validatePrerequisite(String studentId, Event event) {
        if (event == null || event.getTypeDetails() == null || event.getTypeDetails().getPrerequisite() == null) {
            return true;
        }

        Event.Prerequisite prerequisite = event.getTypeDetails().getPrerequisite();
        if (prerequisite.getSubjectCode() == null || prerequisite.getSubjectCode().isBlank()) {
            return true;
        }

        try {
            // Updated table and column names to match standard relational models if needed, 
            // but keeping provided logic structure.
            Integer approvedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ENROLLMENTS WHERE student_id = ? AND NRC IN (SELECT NRC FROM GROUPS WHERE subject_code = ?) AND status = 'Passed'",
                    Integer.class,
                    studentId,
                    prerequisite.getSubjectCode()
            );
            return approvedCount != null && approvedCount > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    public Double loadLatestSemester(String studentId) {
        try {
            // Using the ENROLLMENTS and GROUPS tables from SQL
            String semester = jdbcTemplate.queryForObject(
                    "SELECT semester FROM GROUPS WHERE NRC IN (SELECT NRC FROM ENROLLMENTS WHERE student_id = ?) ORDER BY semester DESC LIMIT 1",
                    String.class,
                    studentId
            );
            if (semester != null && semester.contains("-")) {
                String[] parts = semester.split("-");
                return Double.parseDouble(parts[0]) + (Double.parseDouble(parts[1]) / 10.0);
            }
            return 0.0; 
        } catch (Exception ex) {
            return 0.0;
        }
    }
}
