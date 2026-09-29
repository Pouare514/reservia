package com.reservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Réglages métier externalisés (application.properties, préfixe reservation.*).
 * Plus aucune constante métier en dur dans les services.
 */
@ConfigurationProperties(prefix = "reservation")
public class ReservationProperties {

    /** Délai minimum avant le début pour pouvoir annuler, en minutes. */
    private int delaiAnnulationMinutes = 60;

    public int getDelaiAnnulationMinutes() {
        return delaiAnnulationMinutes;
    }

    public void setDelaiAnnulationMinutes(int delaiAnnulationMinutes) {
        this.delaiAnnulationMinutes = delaiAnnulationMinutes;
    }
}
