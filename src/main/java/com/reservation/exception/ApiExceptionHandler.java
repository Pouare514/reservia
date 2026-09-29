package com.reservation.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Aiguillage HTTP des erreurs métier pour l'API REST uniquement
 * (les pages Thymeleaf attrapent ReservationException dans HomeController).
 * Le switch à motifs est exhaustif grâce au scellé.
 */
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<Map<String, String>> handle(ReservationException ex) {
        int statut = switch (ex) {
            case ReservationIntrouvableException e -> 404;
            case DonneesInvalidesException e -> 400;
            case ReservationConflitException e -> 409;
        };
        return ResponseEntity.status(statut).body(Map.of("erreur", ex.getMessage()));
    }
}
