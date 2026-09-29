package com.reservation.exception;

/** La réservation est déjà annulée. */
public final class ReservationDejaAnnuleeException extends ReservationConflitException {
    public ReservationDejaAnnuleeException(String message) {
        super(message);
    }
}
