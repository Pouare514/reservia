package com.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.dto.BookingRequest;
import com.reservation.exception.CreneauCompletException;
import com.reservation.exception.ReservationIntrouvableException;
import com.reservation.model.*;
import com.reservation.repository.ServiceOfferingRepository;
import com.reservation.service.BookingService;
import com.reservation.service.TimeSlotService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * L'API ne renvoie que des records : on vérifie le JSON, pas les entités.
 * Les erreurs métier typées deviennent 201/404/409 via ApiExceptionHandler.
 */
@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean BookingService bookingService;
    @MockitoBean TimeSlotService slotService;
    @MockitoBean ServiceOfferingRepository serviceRepo;

    private TimeSlot slot;
    private Booking booking;

    @BeforeEach
    void setUp() {
        ServiceOffering service = ServiceOffering.builder().nom("Dune").categorie(Category.CINEMA)
                .dureeMinutes(166).prix(new BigDecimal("9.50")).capaciteDefaut(80).icone("X").build();
        Resource salle = Resource.builder().nom("Salle 1").type("SALLE").categorie(Category.CINEMA).capacite(80).build();
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0);
        slot = TimeSlot.builder().service(service).resource(salle).debut(debut).fin(debut.plusMinutes(166))
                .placesTotales(80).placesReservees(2).statut(SlotStatus.OUVERT).build();
        Customer customer = Customer.builder().nom("Marie").email("marie@ex.fr").build();
        booking = Booking.builder().code("ABCDEF12").customer(customer).slot(slot)
                .nbPlaces(2).statut(BookingStatus.CONFIRMEE).build();
    }

    @Test
    void catalogueServices() throws Exception {
        when(serviceRepo.findAll()).thenReturn(List.of(slot.getService()));
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Dune"));
    }

    @Test
    void creneauxDisponiblesEnRecords() throws Exception {
        when(slotService.listAvailable(Category.CINEMA)).thenReturn(List.of(slot));
        mvc.perform(get("/api/slots/available").param("categorie", "CINEMA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("Dune"))
                .andExpect(jsonPath("$[0].lieu").value("Salle 1"))
                .andExpect(jsonPath("$[0].placesDisponibles").value(78));
    }

    @Test
    void reserverRenvoie201EtConfirmation() throws Exception {
        when(bookingService.reserver(any(BookingRequest.class))).thenReturn(booking);
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(BookingRequest.builder()
                                .slotId(1L).nbPlaces(2).nom("Marie").email("marie@ex.fr").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABCDEF12"))
                .andExpect(jsonPath("$.prixTotal").value(19.00))
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"));
    }

    @Test
    void completRenvoie409() throws Exception {
        when(bookingService.reserver(any(BookingRequest.class)))
                .thenThrow(new CreneauCompletException("Plus assez de places disponibles (reste : 0)."));
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":1,\"nbPlaces\":5,\"nom\":\"N\",\"email\":\"n@ex.fr\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erreur").value("Plus assez de places disponibles (reste : 0)."));
    }

    @Test
    void inconnuRenvoie404() throws Exception {
        when(bookingService.annuler(999L)).thenThrow(new ReservationIntrouvableException("Réservation introuvable."));
        mvc.perform(delete("/api/bookings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erreur").value("Réservation introuvable."));
    }

    @Test
    void annulerRenvoieConfirmation() throws Exception {
        booking.setStatut(BookingStatus.ANNULEE);
        when(bookingService.annuler(1L)).thenReturn(booking);
        mvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABCDEF12"))
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
    }

    @Test
    void reservationsParEmail() throws Exception {
        when(bookingService.reservationsParEmail("marie@ex.fr")).thenReturn(List.of(booking));
        mvc.perform(get("/api/bookings").param("email", "marie@ex.fr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("marie@ex.fr"));
    }
}
