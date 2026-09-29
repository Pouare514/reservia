package com.reservation.exception;

/** Conflit d'état métier → HTTP 409 (complet, fermé, chevauchement, délai, double annulation). */
public sealed abstract class ReservationConflitException extends ReservationException
        permits CreneauCompletException, CreneauFermeException, ChevauchementException,
                DelaiAnnulationException, ReservationDejaAnnuleeException {

    protected ReservationConflitException(String message) {
        super(message);
    }
}
