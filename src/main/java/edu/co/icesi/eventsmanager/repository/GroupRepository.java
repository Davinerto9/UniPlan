package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    List<Group> findBySubjectCode(String subjectCode);
}
