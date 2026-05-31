package edu.co.icesi.eventsmanager.security;

import edu.co.icesi.eventsmanager.document.User;
import edu.co.icesi.eventsmanager.repository.UserRepository;
import edu.co.icesi.eventsmanager.repository.StudentRepository;
import edu.co.icesi.eventsmanager.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, 
                                    StudentRepository studentRepository, 
                                    EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByAuthEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Sync roles with PostgreSQL
        if (user.getInstitutionRef() != null && user.getInstitutionRef().getId() != null) {
            String instId = user.getInstitutionRef().getId();
            boolean isStudent = studentRepository.existsById(instId);
            boolean isEmployee = employeeRepository.existsById(instId);
            
            List<String> newRoles = new ArrayList<>();
            if (isStudent) newRoles.add("STUDENT");
            if (isEmployee) newRoles.add("EMPLOYEE");
            if (user.getRoles() != null && user.getRoles().contains("ADMIN")) {
                newRoles.add("ADMIN");
            }
            if (user.getRoles() != null && user.getRoles().contains("ORGANIZER")) {
                newRoles.add("ORGANIZER");
            }
            
            // Only update if there's a valid change.
            if (!newRoles.isEmpty()) {
                boolean rolesChanged = false;
                if (user.getRoles() == null || user.getRoles().size() != newRoles.size() || !user.getRoles().containsAll(newRoles)) {
                    user.setRoles(newRoles);
                    rolesChanged = true;
                }
                
                // Set primary type (if both, we set it to BOTH, otherwise STUDENT or EMPLOYEE)
                String expectedType = isStudent && isEmployee ? "BOTH" : (isEmployee ? "EMPLOYEE" : "STUDENT");
                if (!expectedType.equals(user.getInstitutionRef().getType())) {
                    user.getInstitutionRef().setType(expectedType);
                    rolesChanged = true;
                }
                
                if (rolesChanged) {
                    userRepository.save(user);
                }
            }
        }
        
        return new CustomUserDetails(user);
    }
}
