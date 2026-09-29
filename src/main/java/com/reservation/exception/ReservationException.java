package com.reservation.exception;

/**
 * Racine métier scellée : le compilateur connaît tous les cas possibles,
 * ce qui permet un aiguillage exhaustif (voir ApiExceptionHandler).
 */
public sealed abstract class ReservationException extends RuntimeException
        permits ReservationIntrouvableException, ReservationConflitException, DonneesInvalidesException {

    protected ReservationException(String message) {
        super(message);
    }
}
