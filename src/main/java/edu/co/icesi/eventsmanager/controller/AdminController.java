package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.service.OrganizerService;
import edu.co.icesi.eventsmanager.service.RegistrationService;
import edu.co.icesi.eventsmanager.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        return "admin_dashboard";
    }

    @GetMapping("/users/new")
    public String newUserForm() {
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
}
