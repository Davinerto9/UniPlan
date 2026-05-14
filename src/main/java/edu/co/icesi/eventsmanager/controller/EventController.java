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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.UserRepository userRepository;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.StudentRepository studentRepository;

    @Autowired
    private edu.co.icesi.eventsmanager.repository.EmployeeRepository employeeRepository;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/home")
    public String home(@RequestParam(required = false) String type,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) String state,
                       Model model) {
        List<Event> events = eventService.getAllEvents();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        if (type != null && !type.isBlank()) {
            events = events.stream().filter(e -> type.equalsIgnoreCase(e.getType())).toList();
        }
        
        if (startDate != null && !startDate.isBlank()) {
            events = events.stream().filter(e -> e.getSchedule() != null && e.getSchedule().getDate().compareTo(startDate) >= 0).toList();
        }
        
        if (endDate != null && !endDate.isBlank()) {
            events = events.stream().filter(e -> e.getSchedule() != null && e.getSchedule().getDate().compareTo(endDate) <= 0).toList();
        }

        if (state != null && !state.isBlank()) {
            events = events.stream().filter(e -> {
                if (e.getSchedule() == null || e.getSchedule().getDate() == null) return false;
                java.time.LocalDate eventDate = java.time.LocalDate.parse(e.getSchedule().getDate());
                if ("UPCOMING".equalsIgnoreCase(state)) return eventDate.isAfter(today);
                if ("IN_PROGRESS".equalsIgnoreCase(state)) return eventDate.isEqual(today);
                if ("FINISHED".equalsIgnoreCase(state)) return eventDate.isBefore(today);
                return true;
            }).toList();
        }

        model.addAttribute("events", events);
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
                            @RequestParam Map<String, String> allParams,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            if (event.getOrganizerId() == null || event.getOrganizerId().isBlank()) {
                event.setOrganizerId(userDetails.getUser().getId());
            }
            
            // Handle extra parameters for additionalProperties
            if (event.getTypeDetails() == null) event.setTypeDetails(new Event.TypeDetails());
            if (event.getTypeDetails().getAdditionalProperties() == null) {
                event.getTypeDetails().setAdditionalProperties(new java.util.HashMap<>());
            }
            
            allParams.forEach((key, value) -> {
                if (key.equals("materialsInput") || key.equals("speakerInfo") || 
                    key.equals("sportType") || key.equals("rulesInput") || 
                    key.equals("cause") || key.equals("requiredHours")) {
                    event.getTypeDetails().getAdditionalProperties().put(key, value);
                }
            });

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
        List<EventRegistration> registrations = registrationService.getRegistrationsByEvent(id);
        
        Map<String, String> userEmails = new java.util.HashMap<>();
        for (EventRegistration reg : registrations) {
            if (!userEmails.containsKey(reg.getUserId())) {
                userRepository.findById(reg.getUserId()).ifPresent(user -> {
                    if (user.getAuth() != null) {
                        userEmails.put(reg.getUserId(), user.getAuth().getEmail());
                    }
                });
            }
        }
        
        model.addAttribute("registrations", registrations);
        model.addAttribute("userEmails", userEmails);
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

    @GetMapping("/event/{id}/export")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public void exportEventRegistrations(@PathVariable String id, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) return;

        List<EventRegistration> regs = registrationService.getRegistrationsByEvent(id);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=event_" + id + "_registrations.csv");
        
        java.io.PrintWriter writer = response.getWriter();
        writer.println("StudentCode,Name,Email,Status,RegisteredAt");
        
        for (EventRegistration reg : regs) {
            edu.co.icesi.eventsmanager.document.User user = userRepository.findById(reg.getUserId()).orElse(null);
            if (user != null && user.getInstitutionRef() != null) {
                String instId = user.getInstitutionRef().getId();
                String name = "N/A";
                String email = user.getAuth() != null ? user.getAuth().getEmail() : "N/A";
                
                if ("STUDENT".equals(user.getInstitutionRef().getType())) {
                    Optional<edu.co.icesi.eventsmanager.entity.Student> s = studentRepository.findById(instId);
                    if (s.isPresent()) {
                        name = s.get().getFirstName() + " " + s.get().getLastName();
                    }
                } else if ("EMPLOYEE".equals(user.getInstitutionRef().getType())) {
                    Optional<edu.co.icesi.eventsmanager.entity.Employee> e = employeeRepository.findById(instId);
                    if (e.isPresent()) {
                        name = e.get().getFirstName() + " " + e.get().getLastName();
                    }
                }
                
                writer.println(String.format("%s,%s,%s,%s,%s", 
                    instId, name, email, reg.getStatus(), reg.getCreatedAt()));
            }
        }
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
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this event.");
            return "redirect:/home";
        }
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("success", "Event deleted successfully.");
        return "redirect:/home";
    }
}
