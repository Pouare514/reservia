package com.reservation.model;

/** Catégorie métier : permet de couvrir cinéma / hôtel / coiffeur avec le même moteur. */
public enum Category {
    CINEMA("Cinéma"),
    HOTEL("Hôtel"),
    COIFFURE("Coiffure");

    private final String label;
    Category(String label) { this.label = label; }
    public String getLabel() { return label; }
}
