package com.reservation.exception;

/** La ressource (salle, chambre, fauteuil) est déjà occupée sur la période. */
public final class ChevauchementException extends ReservationConflitException {
    public ChevauchementException(String message) {
        super(message);
    }
}
