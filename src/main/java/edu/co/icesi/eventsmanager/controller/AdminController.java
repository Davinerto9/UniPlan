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
}
