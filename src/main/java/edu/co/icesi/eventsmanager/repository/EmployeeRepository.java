package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
}
