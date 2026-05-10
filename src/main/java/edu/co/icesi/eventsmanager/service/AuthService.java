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

    public void registerStudent(String studentCode, String email, String password) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("Email is required.");
        }

        if (password == null || password.length() < 6) {
            throw new Exception("Password must have at least 6 characters.");
        }

        // Validate student exists in institutional DB
        Optional<Student> studentOpt = studentRepository.findById(studentCode);
        if (studentOpt.isEmpty()) {
            throw new Exception("Student not found in institutional database.");
        }

        Student student = studentOpt.get();
        if (!student.getEmail().equalsIgnoreCase(email)) {
            throw new Exception("Email does not match institutional records.");
        }

        if (student.getEmail() == null || student.getEmail().isBlank()) {
            throw new Exception("Student institutional record is not active.");
        }

        // Validate not previously registered
        if (userRepository.findByInstitutionRefId(studentCode).isPresent()) {
            throw new Exception("Student is already registered.");
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
        ref.setId(studentCode);
        ref.setType("STUDENT");
        user.setInstitutionRef(ref);

        user.setRoles(List.of("STUDENT"));
        user.setIsActive(true);
        user.setCreatedAt(Instant.now().toString());

        User.AppData appData = new User.AppData();
        appData.setVolunteerHoursCompleted(0.0);
        user.setAppData(appData);

        userRepository.save(user);
    }
}
