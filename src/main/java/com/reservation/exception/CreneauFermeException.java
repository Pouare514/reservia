package com.reservation.exception;

/** Créneau annulé ou déjà passé. */
public final class CreneauFermeException extends ReservationConflitException {
    public CreneauFermeException(String message) {
        super(message);
    }
}
