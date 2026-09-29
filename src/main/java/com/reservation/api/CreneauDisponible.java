package com.reservation.api;

import com.reservation.model.Category;
import com.reservation.model.TimeSlot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Projection allégée d'un créneau pour l'API (pas d'entité, pas de N+1 côté JSON). */
public record CreneauDisponible(
        Long id,
        String service,
        Category categorie,
        String lieu,
        LocalDateTime debut,
        LocalDateTime fin,
        int placesDisponibles,
        int placesTotales,
        BigDecimal prix) {

    public static CreneauDisponible depuis(TimeSlot s) {
        return new CreneauDisponible(
                s.getId(),
                s.getService().getNom(),
                s.getService().getCategorie(),
                s.getResource().getNom(),
                s.getDebut(),
                s.getFin(),
                s.getPlacesDisponibles(),
                s.getPlacesTotales(),
                s.getService().getPrix());
    }
}
