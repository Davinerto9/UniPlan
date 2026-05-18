package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.security.CustomUserDetails;
import edu.co.icesi.eventsmanager.service.RegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public String myRegistrations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Map<String, Object> data = registrationService.getRegistrationsWithTitles(userDetails.getUser().getId());

        model.addAttribute("registrations", data.get("registrations"));
        model.addAttribute("eventTitles", data.get("eventTitles"));
        return "my_registrations";
    }

    @PostMapping("/registrations/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public String cancelRegistration(@PathVariable String id,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            registrationService.cancelRegistration(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Registro cancelado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-registrations";
    }
}
