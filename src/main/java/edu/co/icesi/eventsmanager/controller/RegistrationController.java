package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.document.EventRegistration;
import edu.co.icesi.eventsmanager.security.CustomUserDetails;
import edu.co.icesi.eventsmanager.service.RegistrationService;
import edu.co.icesi.eventsmanager.repository.EventRegistrationRepository;
import edu.co.icesi.eventsmanager.repository.EventStatisticRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.co.icesi.eventsmanager.entity.EventStatistic;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Controller
public class RegistrationController {

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private EventStatisticRepository eventStatisticRepository;

    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public String myRegistrations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<EventRegistration> registrations = registrationRepository.findByUserId(userDetails.getUser().getId());

        Map<String, String> eventTitles = registrations.stream()
            .map(EventRegistration::getEventId)
            .distinct()
            .map(id -> eventStatisticRepository.findById(id).orElse(null))
            .filter(stat -> stat != null)
            .collect(Collectors.toMap(EventStatistic::getEventId, EventStatistic::getEventTitle));

        model.addAttribute("registrations", registrations);
        model.addAttribute("eventTitles", eventTitles);
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
