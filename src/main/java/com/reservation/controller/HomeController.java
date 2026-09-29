package com.reservation.controller;

import com.reservation.dto.BookingRequest;
import com.reservation.exception.ReservationException;
import com.reservation.model.*;
import com.reservation.repository.*;
import com.reservation.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Pages HTML (Thymeleaf). Le REST est dans ApiController. */
@Controller
public class HomeController {

    private final ServiceOfferingRepository serviceRepo;
    private final ResourceRepository resourceRepo;
    private final TimeSlotRepository slotRepo;
    private final BookingRepository bookingRepo;
    private final BookingService bookingService;
    private final TimeSlotService slotService;

    public HomeController(ServiceOfferingRepository serviceRepo, ResourceRepository resourceRepo,
                          TimeSlotRepository slotRepo, BookingRepository bookingRepo,
                          BookingService bookingService, TimeSlotService slotService) {
        this.serviceRepo = serviceRepo;
        this.resourceRepo = resourceRepo;
        this.slotRepo = slotRepo;
        this.bookingRepo = bookingRepo;
        this.bookingService = bookingService;
        this.slotService = slotService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("nbServices", serviceRepo.count());
        model.addAttribute("nbDisponibles", slotRepo.countAvailable());
        model.addAttribute("nbReservations", bookingRepo.countByStatut(BookingStatus.CONFIRMEE));
        // Panaché : 2 prochains créneaux par univers au lieu de 6x le même (coiffeur à 9h...).
        List<TimeSlot> prochains = new ArrayList<>();
        for (Category c : Category.values()) {
            prochains.addAll(slotRepo.findAvailable(c).stream().limit(2).toList());
        }
        prochains.sort(Comparator.comparing(TimeSlot::getDebut));
        model.addAttribute("prochains", prochains);
        return "index";
    }

    @GetMapping("/services")
    public String services(@RequestParam(required = false) Category categorie, Model model) {
        List<ServiceOffering> list = (categorie == null) ? serviceRepo.findAll() : serviceRepo.findByCategorie(categorie);
        model.addAttribute("services", list);
        model.addAttribute("categorie", categorie);
        return "services";
    }

    @GetMapping("/creneaux")
    public String creneaux(@RequestParam(required = false) Category categorie,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           Model model) {
        List<TimeSlot> slots = slotService.listAvailable(categorie);
        if (date != null) {
            slots = slots.stream()
                    .filter(s -> s.getDebut().toLocalDate().equals(date))
                    .toList();
        }
        model.addAttribute("slots", slots);
        model.addAttribute("categorie", categorie);
        model.addAttribute("date", date);
        return "slots";
    }

    @GetMapping("/reserver/{slotId}")
    public String formReservation(@PathVariable Long slotId, Model model, RedirectAttributes ra) {
        TimeSlot slot = slotRepo.findById(slotId).orElse(null);
        if (slot == null) {
            ra.addFlashAttribute("erreur", "Créneau introuvable.");
            return "redirect:/creneaux";
        }
        BookingRequest req = BookingRequest.builder().slotId(slotId).nbPlaces(1).build();
        model.addAttribute("slot", slot);
        model.addAttribute("bookingRequest", req);
        return "booking-form";
    }

    @PostMapping("/reserver/{slotId}")
    public String reserver(@PathVariable Long slotId,
                           @Valid @ModelAttribute("bookingRequest") BookingRequest req,
                           BindingResult br, Model model, RedirectAttributes ra) {
        req.setSlotId(slotId);
        TimeSlot slot = slotRepo.findById(slotId).orElse(null);
        if (slot == null) {
            ra.addFlashAttribute("erreur", "Ce créneau n'existe plus.");
            return "redirect:/creneaux";
        }
        model.addAttribute("slot", slot);
        if (br.hasErrors()) return "booking-form";
        try {
            Booking booking = bookingService.reserver(req);
            ra.addFlashAttribute("succes", "Réservé. Votre code : " + booking.getCode());
            return "redirect:/reservations?email=" + booking.getCustomer().getEmail();
        } catch (ReservationException e) {
            model.addAttribute("erreur", e.getMessage());
            return "booking-form";
        }
    }

    @GetMapping("/reservations")
    public String reservations(@RequestParam(required = false) String email, Model model) {
        List<Booking> list =
                (email == null || email.isBlank()) ? bookingService.toutesReservations()
                        : bookingService.reservationsParEmail(email.trim());
        model.addAttribute("bookings", list);
        model.addAttribute("email", email == null ? "" : email);
        return "bookings";
    }

    @PostMapping("/reservations/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes ra,
                          @RequestParam(required = false) String email) {
        try {
            Booking b = bookingService.annuler(id);
            ra.addFlashAttribute("succes", "Billet " + b.getCode() + " rendu. Les places repartent au guichet.");
        } catch (ReservationException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/reservations" + ((email != null && !email.isBlank()) ? "?email=" + email : "");
    }

    @GetMapping("/admin/creneaux/nouveau")
    public String nouveauCreneau(Model model) {
        model.addAttribute("services", serviceRepo.findAll());
        model.addAttribute("resources", resourceRepo.findAll());
        model.addAttribute("suggestionDebut", LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
        return "slot-form";
    }

    @PostMapping("/admin/creneaux")
    public String creerCreneau(@RequestParam Long serviceId,
                               @RequestParam Long resourceId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
                               @RequestParam(defaultValue = "0") int places,
                               RedirectAttributes ra) {
        try {
            slotService.createSlot(serviceId, resourceId, debut, fin, places <= 0 ? null : places);
            ra.addFlashAttribute("succes", "Créneau créé avec succès.");
            return "redirect:/creneaux";
        } catch (ReservationException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/admin/creneaux/nouveau";
        }
    }

    @PostMapping("/admin/creneaux/{id}/annuler")
    public String annulerCreneau(@PathVariable Long id, RedirectAttributes ra) {
        try {
            slotService.cancelSlot(id);
            ra.addFlashAttribute("succes", "Créneau retiré du guichet.");
        } catch (ReservationException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/creneaux";
    }
}
