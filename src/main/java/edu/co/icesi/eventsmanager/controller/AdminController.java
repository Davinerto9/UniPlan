package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.service.OrganizerService;
import edu.co.icesi.eventsmanager.service.RegistrationService;
import edu.co.icesi.eventsmanager.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private OrganizerService organizerService;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.UserRepository userRepository;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.EventRepository eventRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("eventCount", eventRepository.count());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("regCount", registrationService.getAllRegistrations().size());
        return "admin_dashboard";
    }

    @GetMapping("/reports/popularity")
    public String popularityReport(Model model) {
        model.addAttribute("stats", statisticsService.getAllStatistics());
        return "admin_report_popularity";
    }

    @GetMapping("/reports/volunteers")
    public String volunteersReport(Model model) {
        model.addAttribute("users", userRepository.findAll().stream()
            .filter(u -> u.getAppData() != null)
            .sorted((u1, u2) -> u2.getAppData().getVolunteerHoursCompleted().compareTo(u1.getAppData().getVolunteerHoursCompleted()))
            .limit(10)
            .toList());
        return "admin_report_volunteers";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        return "admin_user_form";
    }

    @PostMapping("/users/save")
    public String saveUser(@RequestParam String email, @RequestParam String password, @RequestParam String role, RedirectAttributes redirectAttributes) {
        try {
            organizerService.registerUser(email, password, role, role); // Usamos role como institution type por defecto
            redirectAttributes.addFlashAttribute("success", "User registered successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/registrations")
    public String allRegistrations(Model model) {
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        return "admin_registrations";
    }

    @GetMapping("/registrations/export")
    public void exportRegistrations(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=registrations.csv");
        
        java.io.PrintWriter writer = response.getWriter();
        writer.println("RegistrationID,EventID,UserID,Status,CreatedAt");
        
        for (edu.co.icesi.eventsmanager.document.EventRegistration reg : registrationService.getAllRegistrations()) {
            writer.println(String.format("%s,%s,%s,%s,%s", 
                reg.getId(), reg.getEventId(), reg.getUserId(), reg.getStatus(), reg.getCreatedAt()));
        }
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin_users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(userId);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/assign-organizer")
    public String assignOrganizerRole(@PathVariable String userId, 
                                     @RequestParam String organizerType,
                                     RedirectAttributes redirectAttributes) {
        try {
            String[] allowedTypes = {"PROFESSOR", "LEADER", "WELLBEING"};
            String upperType = organizerType.toUpperCase();
            boolean isValidType = false;
            for (String type : allowedTypes) {
                if (type.equals(upperType)) {
                    isValidType = true;
                    break;
                }
            }
            
            if (!isValidType) {
                redirectAttributes.addFlashAttribute("error", "Tipo de organizador inválido. Debe ser: PROFESSOR, LEADER o WELLBEING");
                return "redirect:/admin/users";
            }
            
            java.util.Optional<edu.co.icesi.eventsmanager.document.User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/users";
            }
            
            edu.co.icesi.eventsmanager.document.User user = userOpt.get();
            if (user.getRoles() == null) {
                user.setRoles(new java.util.ArrayList<>());
            }
            
            if (!user.getRoles().contains("ORGANIZER")) {
                user.getRoles().add("ORGANIZER");
            }
            
            String roleWithType = "ORGANIZER_" + upperType;
            if (!user.getRoles().contains(roleWithType)) {
                user.getRoles().add(roleWithType);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Rol de organizador (" + upperType + ") asignado exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "El usuario ya tiene el rol de organizador tipo " + upperType);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al asignar rol: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/remove-organizer")
    public String removeOrganizerRole(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<edu.co.icesi.eventsmanager.document.User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/users";
            }
            
            edu.co.icesi.eventsmanager.document.User user = userOpt.get();
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El usuario no tiene roles de organizador.");
                return "redirect:/admin/users";
            }
            
            java.util.List<String> rolesToRemove = new java.util.ArrayList<>();
            for (String role : user.getRoles()) {
                if (role.startsWith("ORGANIZER")) {
                    rolesToRemove.add(role);
                }
            }
            
            if (rolesToRemove.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El usuario no tiene roles de organizador.");
                return "redirect:/admin/users";
            }
            
            user.getRoles().removeAll(rolesToRemove);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Roles de organizador removidos exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al remover rol: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
