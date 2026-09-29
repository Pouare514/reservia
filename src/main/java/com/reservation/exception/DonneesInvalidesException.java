package com.reservation.exception;

/** Données d'entrée invalides (dates, places) → HTTP 400. */
public final class DonneesInvalidesException extends ReservationException {
    public DonneesInvalidesException(String message) {
        super(message);
    }
}
