package com.reservation.exception;

/** Entité demandée inexistante → HTTP 404 (créneau, réservation, service, ressource). */
public final class ReservationIntrouvableException extends ReservationException {
    public ReservationIntrouvableException(String message) {
        super(message);
    }
}
