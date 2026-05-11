package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.service.RegistrationService;
import edu.co.icesi.eventsmanager.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        return "admin_dashboard";
    }

    @GetMapping("/registrations")
    public String allRegistrations(Model model) {
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        return "admin_registrations";
    }
}
