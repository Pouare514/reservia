package com.reservation.exception;

/** Annulation trop tardive (voir reservation.delai-annulation-minutes). */
public final class DelaiAnnulationException extends ReservationConflitException {
    public DelaiAnnulationException(String message) {
        super(message);
    }
}
