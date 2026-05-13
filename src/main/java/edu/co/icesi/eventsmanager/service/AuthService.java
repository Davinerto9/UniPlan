package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.entity.Student;
import edu.co.icesi.eventsmanager.repository.StudentRepository;
import edu.co.icesi.eventsmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.EmployeeRepository employeeRepository;

    public void registerUser(String code, String email, String password, String userType) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("Email is required.");
        }

        if (password == null || password.length() < 6) {
            throw new Exception("Password must have at least 6 characters.");
        }

        if (userType == null || (!userType.equals("STUDENT") && !userType.equals("EMPLOYEE"))) {
            throw new Exception("Invalid user type.");
        }

        // Validate existence in institutional DB
        if ("STUDENT".equals(userType)) {
            Optional<Student> studentOpt = studentRepository.findById(code);
            if (studentOpt.isEmpty()) throw new Exception("Student ID not found in institutional records.");
            if (!studentOpt.get().getEmail().equalsIgnoreCase(email)) throw new Exception("Email does not match institutional records.");
        } else {
            Optional<edu.co.icesi.eventsmanager.entity.Employee> empOpt = employeeRepository.findById(code);
            if (empOpt.isEmpty()) throw new Exception("Employee ID not found in institutional records.");
            if (!empOpt.get().getEmail().equalsIgnoreCase(email)) throw new Exception("Email does not match institutional records.");
        }

        // Validate not previously registered
        if (userRepository.findByInstitutionRefId(code).isPresent()) {
            throw new Exception("User with this ID is already registered.");
        }

        if (userRepository.findByAuthEmail(email).isPresent()) {
            throw new Exception("Email is already in use.");
        }

        // Create User
        User user = new User();
        
        User.Auth auth = new User.Auth();
        auth.setEmail(email);
        auth.setPasswordHash(passwordEncoder.encode(password));
        user.setAuth(auth);

        User.InstitutionRef ref = new User.InstitutionRef();
        ref.setId(code);
        ref.setType(userType);
        user.setInstitutionRef(ref);

        user.setRoles(List.of(userType));
        user.setIsActive(true);
        user.setCreatedAt(Instant.now().toString());

        User.AppData appData = new User.AppData();
        appData.setVolunteerHoursCompleted(0.0);
        user.setAppData(appData);

        userRepository.save(user);
    }
}
