package com.reservation.api;

import com.reservation.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Réponse API : ce que le guichet rend après réservation ou annulation.
 * Les entités JPA ne fuient plus dans le JSON.
 */
public record ConfirmationReservation(
        String code,
        String service,
        String lieu,
        LocalDateTime debut,
        int nbPlaces,
        BigDecimal prixTotal,
        String email,
        String statut) {

    public static ConfirmationReservation depuis(Booking b) {
        var slot = b.getSlot();
        var prix = slot.getService().getPrix();
        return new ConfirmationReservation(
                b.getCode(),
                slot.getService().getNom(),
                slot.getResource().getNom(),
                slot.getDebut(),
                b.getNbPlaces(),
                prix == null ? null : prix.multiply(BigDecimal.valueOf(b.getNbPlaces())),
                b.getCustomer().getEmail(),
                b.getStatut().name());
    }
}
