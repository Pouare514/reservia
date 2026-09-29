package com.reservation.service;

import com.reservation.exception.ChevauchementException;
import com.reservation.exception.DonneesInvalidesException;
import com.reservation.exception.ReservationIntrouvableException;
import com.reservation.model.*;
import com.reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestion des créneaux / disponibilités.
 * Couche service : toute la logique métier (chevauchement, capacité, statuts).
 */
@Service
public class TimeSlotService {

    private final TimeSlotRepository slotRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final ResourceRepository resourceRepository;

    public TimeSlotService(TimeSlotRepository slotRepository,
                           ServiceOfferingRepository serviceRepository,
                           ResourceRepository resourceRepository) {
        this.slotRepository = slotRepository;
        this.serviceRepository = serviceRepository;
        this.resourceRepository = resourceRepository;
    }

    public List<TimeSlot> listAvailable(Category categorie) {
        return slotRepository.findAvailable(categorie);
    }

    public List<TimeSlot> listAll() {
        return slotRepository.findAll().stream()
                .sorted((a, b) -> a.getDebut().compareTo(b.getDebut()))
                .toList();
    }

    @Transactional
    public TimeSlot createSlot(Long serviceId, Long resourceId,
                               LocalDateTime debut, LocalDateTime fin, Integer placesTotales) {
        ServiceOffering service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ReservationIntrouvableException("Service introuvable."));
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ReservationIntrouvableException("Ressource introuvable."));

        if (debut == null || fin == null)
            throw new DonneesInvalidesException("Les dates de début et fin sont obligatoires.");
        if (!fin.isAfter(debut))
            throw new DonneesInvalidesException("La fin doit être après le début.");
        if (debut.isBefore(LocalDateTime.now().minusMinutes(1)))
            throw new DonneesInvalidesException("Impossible de créer un créneau dans le passé.");

        int places = (placesTotales == null || placesTotales <= 0) ? service.getCapaciteDefaut() : placesTotales;
        if (places <= 0)
            throw new DonneesInvalidesException("Le nombre de places doit être positif.");

        // Règle métier : pas de chevauchement sur la même ressource (salle, chambre, coiffeur).
        List<TimeSlot> conflits = slotRepository.findOverlapping(resourceId, debut, fin);
        if (!conflits.isEmpty())
            throw new ChevauchementException("Conflit : la ressource « " + resource.getNom()
                    + " » est déjà occupée sur cette période.");

        TimeSlot slot = TimeSlot.builder()
                .service(service)
                .resource(resource)
                .debut(debut)
                .fin(fin)
                .placesTotales(places)
                .placesReservees(0)
                .statut(SlotStatus.OUVERT)
                .build();
        return slotRepository.save(slot);
    }

    @Transactional
    public TimeSlot cancelSlot(Long slotId) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ReservationIntrouvableException("Créneau introuvable."));
        slot.setStatut(SlotStatus.ANNULE);
        return slotRepository.save(slot);
    }

    @Transactional
    public TimeSlot reopenSlot(Long slotId) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ReservationIntrouvableException("Créneau introuvable."));
        if (slot.getPlacesDisponibles() <= 0) slot.setStatut(SlotStatus.COMPLET);
        else slot.setStatut(SlotStatus.OUVERT);
        return slotRepository.save(slot);
    }
}
