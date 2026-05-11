package edu.co.icesi.eventsmanager.controller;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.document.EventRegistration;
import edu.co.icesi.eventsmanager.document.EventType;
import edu.co.icesi.eventsmanager.security.CustomUserDetails;
import edu.co.icesi.eventsmanager.service.EventService;
import edu.co.icesi.eventsmanager.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

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

    @GetMapping("/events/new")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String createEventForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("eventTypes", EventType.values());
        return "event_form";
    }

    @PostMapping("/events/save")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String saveEvent(@ModelAttribute Event event,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            if (event.getOrganizerId() == null || event.getOrganizerId().isBlank()) {
                event.setOrganizerId(userDetails.getUser().getId());
            }
            eventService.saveEvent(event);
            redirectAttributes.addFlashAttribute("success", "Event saved successfully.");
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/events/new";
        }
    }

    @GetMapping("/event/{id}")
    public String eventDetails(@PathVariable String id, Model model) {
        Optional<Event> event = eventService.getEventById(id);
        if (event.isEmpty()) return "redirect:/home";
        model.addAttribute("event", event.get());
        return "event_detail";
    }

    @PostMapping("/event/{id}/register")
    @PreAuthorize("hasRole('STUDENT')")
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

    @GetMapping("/event/{id}/edit")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String editEventForm(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOptional = eventService.getEventById(id);
        if (eventOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Event not found.");
            return "redirect:/home";
        }
        Event event = eventOptional.get();
        if (!userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))
                && !userDetails.getUser().getId().equals(event.getOrganizerId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this event.");
            return "redirect:/home";
        }
        model.addAttribute("event", event);
        model.addAttribute("eventTypes", EventType.values());
        return "event_form";
    }

    @GetMapping("/event/{id}/registrations")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String listRegistrations(@PathVariable String id, Model model) {
        model.addAttribute("registrations", registrationService.getRegistrationsByEvent(id));
        model.addAttribute("eventId", id);
        return "event_registrations";
    }

    @PostMapping("/event/{eventId}/registrations/{regId}/attend")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String markAttendance(@PathVariable String eventId,
                                 @PathVariable String regId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            registrationService.markAttendance(regId, userDetails.getUser().getId());
            redirectAttributes.addFlashAttribute("success", "Attendance marked successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/event/" + eventId + "/registrations";
    }

    @PostMapping("/event/{id}/delete")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String deleteEvent(@PathVariable String id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        Optional<Event> eventOptional = eventService.getEventById(id);
        if (eventOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Event not found.");
            return "redirect:/home";
        }
        Event event = eventOptional.get();
        if (!userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))
                && !userDetails.getUser().getId().equals(event.getOrganizerId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this event.");
            return "redirect:/home";
        }
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("success", "Event deleted successfully.");
        return "redirect:/home";
    }
}
