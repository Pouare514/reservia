package com.reservation.controller;

import com.reservation.dto.BookingRequest;
import com.reservation.exception.CreneauCompletException;
import com.reservation.model.*;
import com.reservation.repository.*;
import com.reservation.service.BookingService;
import com.reservation.service.TimeSlotService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Les pages rendent vraiment (Thymeleaf réel) : un template cassé fait échouer le test.
 */
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ServiceOfferingRepository serviceRepo;
    @MockitoBean ResourceRepository resourceRepo;
    @MockitoBean TimeSlotRepository slotRepo;
    @MockitoBean BookingRepository bookingRepo;
    @MockitoBean BookingService bookingService;
    @MockitoBean TimeSlotService slotService;

    private TimeSlot slot;

    @BeforeEach
    void setUp() {
        ServiceOffering service = ServiceOffering.builder().nom("Dune").categorie(Category.CINEMA)
                .description("SF").dureeMinutes(166).prix(new BigDecimal("9.50"))
                .capaciteDefaut(80).icone("X").build();
        Resource salle = Resource.builder().nom("Salle 1").type("SALLE").categorie(Category.CINEMA).capacite(80).build();
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0);
        slot = TimeSlot.builder().service(service).resource(salle).debut(debut).fin(debut.plusMinutes(166))
                .placesTotales(80).placesReservees(0).statut(SlotStatus.OUVERT).build();
    }

    @Test
    void accueilAfficheLeGuichet() throws Exception {
        when(serviceRepo.count()).thenReturn(6L);
        when(slotRepo.countAvailable()).thenReturn(31L);
        when(bookingRepo.countByStatut(BookingStatus.CONFIRMEE)).thenReturn(0L);
        when(slotRepo.findAvailable(any())).thenReturn(List.of(slot));
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Au guichet")));
    }

    @Test
    void billetsAffichentLesSouches() throws Exception {
        when(slotService.listAvailable(null)).thenReturn(List.of(slot));
        mvc.perform(get("/creneaux"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Billets disponibles")));
    }

    @Test
    void programmeListeLesServices() throws Exception {
        when(serviceRepo.findAll()).thenReturn(List.of(slot.getService()));
        mvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Programme")));
    }

    @Test
    void mesPlacesAvecFiltreEmail() throws Exception {
        when(bookingService.reservationsParEmail("a@ex.fr")).thenReturn(List.of());
        mvc.perform(get("/reservations").param("email", "a@ex.fr"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mes places")));
    }

    @Test
    void formulaireReservation() throws Exception {
        when(slotRepo.findById(1L)).thenReturn(Optional.of(slot));
        mvc.perform(get("/reserver/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Vos coordonnées")));
    }

    @Test
    void reserverRedirigeAvecLeCode() throws Exception {
        Customer customer = Customer.builder().nom("Marie").email("marie@ex.fr").build();
        Booking booking = Booking.builder().code("ABCDEF12").customer(customer).slot(slot)
                .nbPlaces(2).statut(BookingStatus.CONFIRMEE).build();
        when(slotRepo.findById(1L)).thenReturn(Optional.of(slot));
        when(bookingService.reserver(any(BookingRequest.class))).thenReturn(booking);
        mvc.perform(post("/reserver/1").param("nom", "Marie").param("email", "marie@ex.fr").param("nbPlaces", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations?email=marie@ex.fr"));
    }

    @Test
    void completReafficheLeFormulaireAvecErreur() throws Exception {
        when(slotRepo.findById(1L)).thenReturn(Optional.of(slot));
        when(bookingService.reserver(any(BookingRequest.class)))
                .thenThrow(new CreneauCompletException("Plus assez de places."));
        mvc.perform(post("/reserver/1").param("nom", "Marie").param("email", "marie@ex.fr").param("nbPlaces", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Plus assez de places.")));
    }

    @Test
    void reserverCreneauInconnuRedirige() throws Exception {
        when(slotRepo.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(get("/reserver/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creneaux"));
    }

    @Test
    void mesPlacesSansFiltreAfficheTout() throws Exception {
        when(bookingService.toutesReservations()).thenReturn(List.of());
        mvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mes places")));
    }

    @Test
    void rendreUnePlaceRedirige() throws Exception {
        Customer customer = Customer.builder().nom("Marie").email("marie@ex.fr").build();
        Booking booking = Booking.builder().code("ABCDEF12").customer(customer).slot(slot)
                .nbPlaces(1).statut(BookingStatus.ANNULEE).build();
        when(bookingService.annuler(1L)).thenReturn(booking);
        mvc.perform(post("/reservations/1/annuler").param("email", "marie@ex.fr"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations?email=marie@ex.fr"));
    }

    @Test
    void rendreTropTardReafficheErreur() throws Exception {
        when(bookingService.annuler(1L))
                .thenThrow(new com.reservation.exception.DelaiAnnulationException("Délai dépassé."));
        mvc.perform(post("/reservations/1/annuler"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations"));
    }

    @Test
    void ouvrirUnCreneauRedirigeVersLesBillets() throws Exception {
        when(serviceRepo.findAll()).thenReturn(List.of(slot.getService()));
        when(resourceRepo.findAll()).thenReturn(List.of(slot.getResource()));
        mvc.perform(get("/admin/creneaux/nouveau"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ouvrir un créneau")));

        when(slotService.createSlot(any(), any(), any(), any(), any())).thenReturn(slot);
        mvc.perform(post("/admin/creneaux")
                        .param("serviceId", "1").param("resourceId", "1")
                        .param("debut", "2026-10-05T10:00").param("fin", "2026-10-05T10:30")
                        .param("places", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creneaux"));
    }

    @Test
    void chevauchementReafficheLeFormulaireAdmin() throws Exception {
        when(slotService.createSlot(any(), any(), any(), any(), any()))
                .thenThrow(new com.reservation.exception.ChevauchementException("Conflit."));
        mvc.perform(post("/admin/creneaux")
                        .param("serviceId", "1").param("resourceId", "1")
                        .param("debut", "2026-10-05T10:00").param("fin", "2026-10-05T10:30")
                        .param("places", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/creneaux/nouveau"));
    }

    @Test
    void retirerUnCreneauRedirige() throws Exception {
        when(slotService.cancelSlot(1L)).thenReturn(slot);
        mvc.perform(post("/admin/creneaux/1/annuler"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creneaux"));
    }
}
