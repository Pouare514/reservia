package com.reservation.controller;

import com.reservation.api.ConfirmationReservation;
import com.reservation.api.CreneauDisponible;
import com.reservation.dto.BookingRequest;
import com.reservation.model.*;
import com.reservation.repository.*;
import com.reservation.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST : mêmes règles métier que les pages, exposées en JSON
 * via des records (les entités JPA ne fuient pas).
 * Les erreurs métier sont converties en HTTP par ApiExceptionHandler.
 * Doc interactive : /swagger-ui.html
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Billetterie", description = "Disponibilités, réservations, annulations")
public class ApiController {

    private final BookingService bookingService;
    private final TimeSlotService slotService;
    private final ServiceOfferingRepository serviceRepo;

    public ApiController(BookingService bookingService, TimeSlotService slotService,
                         ServiceOfferingRepository serviceRepo) {
        this.bookingService = bookingService;
        this.slotService = slotService;
        this.serviceRepo = serviceRepo;
    }

    @Operation(summary = "Catalogue des services")
    @GetMapping("/services")
    public List<ServiceOffering> services() {
        return serviceRepo.findAll();
    }

    @Operation(summary = "Créneaux disponibles, filtre univers optionnel")
    @GetMapping("/slots/available")
    public List<CreneauDisponible> disponibles(@RequestParam(required = false) Category categorie) {
        return slotService.listAvailable(categorie).stream().map(CreneauDisponible::depuis).toList();
    }

    @Operation(summary = "Réserver des places (201, corps = confirmation)")
    @PostMapping("/bookings")
    public ResponseEntity<ConfirmationReservation> reserver(@Valid @RequestBody BookingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ConfirmationReservation.depuis(bookingService.reserver(req)));
    }

    @Operation(summary = "Annuler une réservation (places libérées)")
    @DeleteMapping("/bookings/{id}")
    public ConfirmationReservation annuler(@PathVariable Long id) {
        return ConfirmationReservation.depuis(bookingService.annuler(id));
    }

    @Operation(summary = "Réservations d'un client, par email")
    @GetMapping("/bookings")
    public List<ConfirmationReservation> parEmail(@RequestParam String email) {
        return bookingService.reservationsParEmail(email).stream().map(ConfirmationReservation::depuis).toList();
    }
}
