package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.security.CustomUserDetails;
import edu.co.icesi.eventsmanager.service.EventService;
import edu.co.icesi.eventsmanager.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "home";
    }

    @GetMapping("/event/{id}")
    public String eventDetails(@PathVariable String id, Model model) {
        Event event = eventService.getEventById(id).orElse(null);
        if (event == null) return "redirect:/home";
        model.addAttribute("event", event);
        return "event_detail";
    }

    @PostMapping("/event/{id}/register")
    public String registerForEvent(@PathVariable String id, 
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            registrationService.registerToEvent(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Successfully registered for event.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/event/" + id;
    }
}
