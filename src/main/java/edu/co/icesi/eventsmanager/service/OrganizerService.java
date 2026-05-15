package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.document.Organizer;
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
    private edu.co.icesi.eventsmanager.repository.EmployeeRepository employeeRepository;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Organizer registerOrganizer(String code, String email, String password, String organizerType,
                                       Double facultyCode, Double departmentCode, String areaCode,
                                       Integer academicProgramCode, Integer semester, String groupRepresented,
                                       String administrativeArea, String position) throws Exception {
        if (code == null || code.isBlank()) {
            throw new Exception("Institutional code is required.");
        }
        if (email == null || email.isBlank()) {
            throw new Exception("Email is required.");
        }
        if (password == null || password.length() < 8) {
            throw new Exception("Password must have at least 8 characters.");
        }
        if (organizerType == null || !ALLOWED_ORGANIZER_TYPES.contains(organizerType.toUpperCase())) {
            throw new Exception("Organizer type must be one of: PROFESSOR, LEADER, WELLBEING.");
        }

        // Validate existence in institutional DB
        if ("PROFESSOR".equals(organizerType.toUpperCase())) {
            edu.co.icesi.eventsmanager.entity.Employee emp = employeeRepository.findById(code)
                    .orElseThrow(() -> new Exception("Professor ID not found in institutional records."));
            if (!emp.getEmail().equalsIgnoreCase(email)) {
                throw new Exception("Email does not match institutional records for this Professor.");
            }
        } else if ("LEADER".equals(organizerType.toUpperCase())) {
            edu.co.icesi.eventsmanager.entity.Student student = studentRepository.findById(code)
                    .orElseThrow(() -> new Exception("Student ID not found in institutional records."));
            if (!student.getEmail().equalsIgnoreCase(email)) {
                throw new Exception("Email does not match institutional records for this Student.");
            }
        }
        
        Optional<User> existingUserOpt = userRepository.findByAuthEmail(email);
        User savedUser;
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getInstitutionRef() == null || existingUser.getInstitutionRef().getId() == null
                    || !existingUser.getInstitutionRef().getId().equals(code)) {
                throw new Exception("Email is already in use by another institutional account.");
            }

            List<String> roles = existingUser.getRoles() != null ? new ArrayList<>(existingUser.getRoles()) : new ArrayList<>();
            if (!roles.contains("ORGANIZER")) {
                roles.add("ORGANIZER");
            }
            String organizerRole = "ORGANIZER_" + organizerType.toUpperCase();
            if (!roles.contains(organizerRole)) {
                roles.add(organizerRole);
            }
            existingUser.setRoles(roles);
            existingUser.setIsActive(true);
            savedUser = userRepository.save(existingUser);
        } else {
            User user = new User();
            User.Auth auth = new User.Auth();
            auth.setEmail(email);
            auth.setPasswordHash(passwordEncoder.encode(password));
            user.setAuth(auth);

            User.InstitutionRef ref = new User.InstitutionRef();
            ref.setId(code);
            ref.setType("EMPLOYEE");
            if ("LEADER".equals(organizerType.toUpperCase())) {
                ref.setType("STUDENT");
            }
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

            savedUser = userRepository.save(user);
        }

        if (organizerRepository.findByUserId(savedUser.getId()).isPresent()) {
            throw new Exception("This user is already registered as an organizer.");
        }

        Organizer organizer = new Organizer();
        organizer.setUserId(savedUser.getId());
        organizer.setOrganizerType(organizerType.toUpperCase());
        
        Organizer.TypeDetails typeDetails = new Organizer.TypeDetails();
        // Only save what is NOT in institutional DB
        if ("PROFESSOR".equals(organizerType.toUpperCase())) {
            // facultyCode and departmentCode are often in Employee table, but areaCode might be specific
            typeDetails.setAreaCode(areaCode);
        } else if ("LEADER".equals(organizerType.toUpperCase())) {
            // program is in Student/Academic tables, but groupRepresented and semester are specific/transient
            typeDetails.setSemester(semester);
            typeDetails.setGroupRepresented(groupRepresented);
        } else if ("WELLBEING".equals(organizerType.toUpperCase())) {
            typeDetails.setAdministrativeArea(administrativeArea);
            typeDetails.setPosition(position);
        }
        
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
        ref.setId(null);
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
