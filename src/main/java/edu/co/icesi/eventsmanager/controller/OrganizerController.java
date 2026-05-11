package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.security.CustomUserDetails;
import edu.co.icesi.eventsmanager.service.EventService;
import edu.co.icesi.eventsmanager.service.OrganizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrganizerController {

    @Autowired
    private OrganizerService organizerService;

    @Autowired
    private EventService eventService;

    @GetMapping("/organizer/dashboard")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public String organizerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("events", eventService.getEventsByOrganizer(userDetails.getUser().getId()));
        return "organizer_dashboard";
    }

    @GetMapping("/register-organizer")
    @PreAuthorize("hasRole('ADMIN')")
    public String organizerRegistrationForm(Model model) {
        return "organizer_register";
    }

    @PostMapping("/register-organizer")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerOrganizer(@RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String organizerType,
                                    @RequestParam(required = false) Double facultyCode,
                                    @RequestParam(required = false) Double departmentCode,
                                    @RequestParam(required = false) String areaCode,
                                    RedirectAttributes redirectAttributes) {
        try {
            organizerService.registerOrganizer(email, password, organizerType, facultyCode, departmentCode, areaCode);
            redirectAttributes.addFlashAttribute("success", "Organizer account created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/register-organizer";
    }
}
