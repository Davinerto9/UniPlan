package edu.co.icesi.eventsmanager.config;

import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final edu.co.icesi.eventsmanager.repository.OrganizerRepository organizerRepository;
    private final edu.co.icesi.eventsmanager.repository.EventRepository eventRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                           edu.co.icesi.eventsmanager.repository.OrganizerRepository organizerRepository,
                           edu.co.icesi.eventsmanager.repository.EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizerRepository = organizerRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for initial data...");
        
        // Inspect problematic student user
        String studentEmail = "laura.hernandez@univcali.edu.co";
        userRepository.findByAuthEmail(studentEmail).ifPresent(user -> {
            edu.co.icesi.eventsmanager.security.CustomUserDetails details = new edu.co.icesi.eventsmanager.security.CustomUserDetails(user);
            boolean isOrganizer = organizerRepository.findByUserId(user.getId()).isPresent();
            long ownedEvents = eventRepository.findAll().stream().filter(e -> user.getId().equals(e.getOrganizerId())).count();
            log.info("Inspecting user {}: Roles={}, Authorities={}, IsActive={}, RegisteredInOrganizerTable={}, OwnedEvents={}", 
                studentEmail, user.getRoles(), details.getAuthorities(), user.getIsActive(), isOrganizer, ownedEvents);
        });

        // Initial Admin Creation
        String adminEmail = "admin@uniplan.edu.co";
        java.util.Optional<User> existingAdmin = userRepository.findByAuthEmail(adminEmail);
        
        if (existingAdmin.isEmpty()) {
            log.info("No admin found. Creating initial admin: {}", adminEmail);
            User admin = new User();
            User.Auth auth = new User.Auth();
            auth.setEmail(adminEmail);
            auth.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setAuth(auth);

            User.InstitutionRef ref = new User.InstitutionRef();
            ref.setId("ADMIN001");
            ref.setType("ADMIN");
            admin.setInstitutionRef(ref);

            admin.setRoles(List.of("ADMIN"));
            admin.setIsActive(true);
            admin.setCreatedAt(Instant.now().toString());

            User.AppData appData = new User.AppData();
            appData.setVolunteerHoursCompleted(0.0);
            admin.setAppData(appData);

            userRepository.save(admin);
            log.info("Initial Admin created: {} / admin123", adminEmail);
        } else {
            User admin = existingAdmin.get();
            // Ensure admin has correct ADMIN role format (String, not Object)
            if (admin.getRoles() == null || admin.getRoles().isEmpty() || !(admin.getRoles().get(0) instanceof String) || !admin.getRoles().contains("ADMIN")) {
                log.info("Updating admin roles to correct format...");
                admin.setRoles(List.of("ADMIN"));
                userRepository.save(admin);
            }
        }
    }
}
