package com.reservation.service;

import com.reservation.dto.BookingRequest;
import com.reservation.exception.ReservationException;
import com.reservation.model.*;
import com.reservation.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration du cœur métier (H2 en mémoire).
 * Couvre : réservation OK, surréservation, annulation, double annulation, délai.
 */
@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired BookingService bookingService;
    @Autowired TimeSlotService slotService;
    @Autowired ServiceOfferingRepository services;
    @Autowired ResourceRepository resources;
    @Autowired TimeSlotRepository slots;
    @Autowired BookingRepository bookings;
    @Autowired CustomerRepository customers;

    private TimeSlot slot;

    @BeforeEach
    void setUp() {
        ServiceOffering service = services.save(ServiceOffering.builder()
                .nom("Test Ciné").categorie(Category.CINEMA).dureeMinutes(120)
                .prix(new BigDecimal("10.00")).capaciteDefaut(5).icone("🎬").build());
        Resource salle = resources.save(Resource.builder()
                .nom("Salle Test").type("SALLE").categorie(Category.CINEMA).capacite(5).build());
        slot = slotService.createSlot(service.getId(), salle.getId(),
                LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(1).withHour(22).withMinute(0).withSecond(0).withNano(0),
                5);
    }

    private BookingRequest req(String email, int places) {
        return BookingRequest.builder().slotId(slot.getId()).nbPlaces(places)
                .nom("Testeur").email(email).telephone("0600000000").build();
    }

    @Test
    @DisplayName("Réservation OK : décrémente les places et génère un code")
    void reservationOk() {
        Booking b = bookingService.reserver(req("ok@test.fr", 2));

        assertThat(b.getId()).isNotNull();
        assertThat(b.getCode()).hasSize(8);
        assertThat(b.getStatut()).isEqualTo(BookingStatus.CONFIRMEE);
        assertThat(slots.findById(slot.getId()).orElseThrow().getPlacesReservees()).isEqualTo(2);
    }

    @Test
    @DisplayName("Surréservation refusée")
    void surReservationRefusee() {
        bookingService.reserver(req("a@test.fr", 4));
        assertThatThrownBy(() -> bookingService.reserver(req("b@test.fr", 2)))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("places");
    }

    @Test
    @DisplayName("Créneau passe à COMPLET quand plein")
    void statutComplet() {
        bookingService.reserver(req("full@test.fr", 5));
        assertThat(slots.findById(slot.getId()).orElseThrow().getStatut()).isEqualTo(SlotStatus.COMPLET);
    }

    @Test
    @DisplayName("Annulation libère les places et rouvre le créneau")
    void annulationLiberePlaces() {
        Booking b = bookingService.reserver(req("cancel@test.fr", 5));
        assertThat(slots.findById(slot.getId()).orElseThrow().getStatut()).isEqualTo(SlotStatus.COMPLET);

        bookingService.annuler(b.getId());

        TimeSlot maj = slots.findById(slot.getId()).orElseThrow();
        assertThat(maj.getPlacesReservees()).isEqualTo(0);
        assertThat(maj.getStatut()).isEqualTo(SlotStatus.OUVERT);
        assertThat(bookings.findById(b.getId()).orElseThrow().getStatut()).isEqualTo(BookingStatus.ANNULEE);
    }

    @Test
    @DisplayName("Double annulation refusée")
    void doubleAnnulationRefusee() {
        Booking b = bookingService.reserver(req("double@test.fr", 1));
        bookingService.annuler(b.getId());
        assertThatThrownBy(() -> bookingService.annuler(b.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("déjà annulée");
    }

    @Test
    @DisplayName("Annulation impossible moins d'1h avant")
    void delaiAnnulation() {
        ServiceOffering s = services.save(ServiceOffering.builder().nom("Express").categorie(Category.COIFFURE)
                .dureeMinutes(30).prix(new BigDecimal("10")).capaciteDefaut(1).build());
        Resource r = resources.save(Resource.builder().nom("Fauteuil X").type("COIFFEUR").categorie(Category.COIFFURE).build());
        TimeSlot imminent = slots.save(TimeSlot.builder().service(s).resource(r)
                .debut(LocalDateTime.now().plusMinutes(30)).fin(LocalDateTime.now().plusMinutes(60))
                .placesTotales(1).placesReservees(0).statut(SlotStatus.OUVERT).build());
        Booking b = bookingService.reserver(BookingRequest.builder().slotId(imminent.getId())
                .nbPlaces(1).nom("Pressé").email("presse@test.fr").build());

        assertThatThrownBy(() -> bookingService.annuler(b.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("délai");
    }
}
