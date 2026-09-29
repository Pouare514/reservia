package com.reservation.exception;

/** Plus assez de places sur le créneau. */
public final class CreneauCompletException extends ReservationConflitException {
    public CreneauCompletException(String message) {
        super(message);
    }
}
