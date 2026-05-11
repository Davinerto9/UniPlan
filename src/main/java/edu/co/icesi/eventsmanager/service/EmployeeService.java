package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.entity.Employee;
import edu.co.icesi.eventsmanager.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }

    public Employee findByEmailAndDepartment(String email, String departmentCode) {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getEmail() != null && emp.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
