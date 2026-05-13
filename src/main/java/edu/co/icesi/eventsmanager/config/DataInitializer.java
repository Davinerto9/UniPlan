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

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for initial data...");
        // Initial Admin Creation
        String adminEmail = "admin@uniplan.edu.co";
        if (userRepository.findByAuthEmail(adminEmail).isEmpty()) {
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
            log.info("Admin already exists: {}", adminEmail);
        }
    }
}
