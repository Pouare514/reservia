package com.reservation.service;

import com.reservation.config.ReservationProperties;
import com.reservation.dto.BookingRequest;
import com.reservation.exception.*;
import com.reservation.model.*;
import com.reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cœur métier : réservations + annulations, 100% transactionnel.
 * Le verrou pessimiste sur le créneau garantit qu'on ne dépasse jamais la capacité,
 * même avec des requêtes concurrentes. Les erreurs sont typées (hiérarchie scellée).
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final TimeSlotRepository slotRepository;
    private final ReservationProperties properties;

    public BookingService(BookingRepository bookingRepository,
                          CustomerRepository customerRepository,
                          TimeSlotRepository slotRepository,
                          ReservationProperties properties) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.slotRepository = slotRepository;
        this.properties = properties;
    }

    @Transactional
    public Booking reserver(BookingRequest req) {
        if (req.getNbPlaces() <= 0)
            throw new DonneesInvalidesException("Nombre de places invalide.");

        // Verrou pessimiste : les autres transactions attendent la fin de celle-ci.
        TimeSlot slot = slotRepository.findByIdForUpdate(req.getSlotId())
                .orElseThrow(() -> new ReservationIntrouvableException("Créneau introuvable."));

        if (slot.getStatut() == SlotStatus.ANNULE)
            throw new CreneauFermeException("Ce créneau a été annulé.");
        if (slot.isPasse())
            throw new CreneauFermeException("Ce créneau est déjà passé.");
        if (slot.getStatut() == SlotStatus.COMPLET || slot.getPlacesDisponibles() < req.getNbPlaces())
            throw new CreneauCompletException("Plus assez de places disponibles (reste : "
                    + slot.getPlacesDisponibles() + ").");

        Customer customer = customerRepository.findByEmailIgnoreCase(req.getEmail().trim())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .nom(req.getNom().trim())
                        .email(req.getEmail().trim().toLowerCase())
                        .telephone(req.getTelephone())
                        .build()));
        if (!customer.getNom().equalsIgnoreCase(req.getNom().trim())) {
            customer.setNom(req.getNom().trim());
            customer = customerRepository.save(customer);
        }

        Booking booking = Booking.builder()
                .customer(customer)
                .slot(slot)
                .nbPlaces(req.getNbPlaces())
                .statut(BookingStatus.CONFIRMEE)
                .code(Booking.genererCode())
                .creeLe(LocalDateTime.now())
                .build();

        slot.setPlacesReservees(slot.getPlacesReservees() + req.getNbPlaces());
        if (slot.getPlacesDisponibles() <= 0) slot.setStatut(SlotStatus.COMPLET);
        slotRepository.save(slot);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking annuler(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ReservationIntrouvableException("Réservation introuvable."));
        if (booking.getStatut() == BookingStatus.ANNULEE)
            throw new ReservationDejaAnnuleeException("Cette réservation est déjà annulée.");

        TimeSlot slot = slotRepository.findByIdForUpdate(booking.getSlot().getId())
                .orElseThrow(() -> new ReservationIntrouvableException("Créneau introuvable."));

        int delai = properties.getDelaiAnnulationMinutes();
        if (LocalDateTime.now().plusMinutes(delai).isAfter(slot.getDebut()))
            throw new DelaiAnnulationException("Annulation impossible : délai dépassé (limite "
                    + delai + " min avant le début).");

        booking.setStatut(BookingStatus.ANNULEE);
        booking.setAnnuleLe(LocalDateTime.now());

        slot.setPlacesReservees(Math.max(0, slot.getPlacesReservees() - booking.getNbPlaces()));
        if (slot.getStatut() == SlotStatus.COMPLET && slot.getPlacesDisponibles() > 0
                && slot.getStatut() != SlotStatus.ANNULE) {
            slot.setStatut(SlotStatus.OUVERT);
        }
        slotRepository.save(slot);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> reservationsParEmail(String email) {
        return bookingRepository.findByCustomerEmailIgnoreCaseOrderByCreeLeDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Booking> toutesReservations() {
        return bookingRepository.findAllByOrderByCreeLeDesc();
    }
}
