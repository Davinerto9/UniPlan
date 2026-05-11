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
        if (student.get().getIsActive() != null && !student.get().getIsActive()) {
            throw new Exception("Student account is not active.");
        }
        if ("INACTIVE".equalsIgnoreCase(student.get().getAcademicStatus()) || "WITHDRAWN".equalsIgnoreCase(student.get().getAcademicStatus())) {
            throw new Exception("Student academic status does not permit event registration.");
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
            Integer approvedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM historial_academico WHERE estudiante_id = ? AND materia_codigo = ? AND estado IN ('APROBADO','APROBADA')",
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
            Integer semester = jdbcTemplate.queryForObject(
                    "SELECT semestre FROM matriculas WHERE estudiante_id = ? ORDER BY semestre DESC LIMIT 1",
                    Integer.class,
                    studentId
            );
            return semester != null ? semester.doubleValue() : 0.0;
        } catch (Exception ex) {
            return 0.0;
        }
    }
}
