package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Organizer;
import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.repository.OrganizerRepository;
import edu.co.icesi.eventsmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizerService {

    private static final List<String> ALLOWED_ORGANIZER_TYPES = List.of("PROFESSOR", "LEADER", "WELLBEING");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Organizer registerOrganizer(String email, String password, String organizerType,
                                       Double facultyCode, Double departmentCode, String areaCode) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("Email is required.");
        }
        if (password == null || password.length() < 8) {
            throw new Exception("Password must have at least 8 characters.");
        }
        if (organizerType == null || !ALLOWED_ORGANIZER_TYPES.contains(organizerType.toUpperCase())) {
            throw new Exception("Organizer type must be one of: PROFESSOR, LEADER, WELLBEING.");
        }

        if (userRepository.findByAuthEmail(email).isPresent()) {
            throw new Exception("Email is already in use.");
        }

        User user = new User();
        User.Auth auth = new User.Auth();
        auth.setEmail(email);
        auth.setPasswordHash(passwordEncoder.encode(password));
        user.setAuth(auth);

        User.InstitutionRef ref = new User.InstitutionRef();
        ref.setId(null);
        ref.setType("ORGANIZER");
        user.setInstitutionRef(ref);

        List<String> roles = new ArrayList<>();
        roles.add("ORGANIZER");
        roles.add("ORGANIZER_" + organizerType.toUpperCase());
        user.setRoles(roles);
        user.setIsActive(true);
        user.setCreatedAt(Instant.now().toString());

        User.AppData appData = new User.AppData();
        appData.setVolunteerHoursCompleted(0.0);
        user.setAppData(appData);

        User savedUser = userRepository.save(user);

        Organizer organizer = new Organizer();
        organizer.setUserId(savedUser.getId());
        organizer.setOrganizerType(organizerType.toUpperCase());
        Organizer.TypeDetails typeDetails = new Organizer.TypeDetails();
        typeDetails.setFacultyCode(facultyCode);
        typeDetails.setDepartmentCode(departmentCode);
        typeDetails.setAreaCode(areaCode);
        organizer.setTypeDetails(typeDetails);
        organizer.setIsActive(true);
        return organizerRepository.save(organizer);
    }

    public User registerUser(String email, String password, String role, String institutionType) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("Email is required.");
        }
        if (password == null || password.length() < 8) {
            throw new Exception("Password must have at least 8 characters.");
        }

        if (userRepository.findByAuthEmail(email).isPresent()) {
            throw new Exception("Email is already in use.");
        }

        User user = new User();
        User.Auth auth = new User.Auth();
        auth.setEmail(email);
        auth.setPasswordHash(passwordEncoder.encode(password));
        user.setAuth(auth);

        User.InstitutionRef ref = new User.InstitutionRef();
        ref.setId(null); // ID de PostgreSQL opcional para registro manual inicial
        ref.setType(institutionType.toUpperCase());
        user.setInstitutionRef(ref);

        List<String> roles = new ArrayList<>();
        roles.add(role.toUpperCase());
        user.setRoles(roles);
        user.setIsActive(true);
        user.setCreatedAt(Instant.now().toString());

        User.AppData appData = new User.AppData();
        appData.setVolunteerHoursCompleted(0.0);
        user.setAppData(appData);

        return userRepository.save(user);
    }

    public Optional<Organizer> findByUserId(String userId) {
        return organizerRepository.findByUserId(userId);
    }
}
