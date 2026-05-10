package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Enrollment.EnrollmentId> {
    boolean existsByStudentIdAndNRC(String studentId, String nrc);
}
