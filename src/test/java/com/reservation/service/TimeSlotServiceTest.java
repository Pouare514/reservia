package com.reservation.service;

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

/** Tests des disponibilités : chevauchement, dates invalides, capacité. */
@SpringBootTest
@Transactional
class TimeSlotServiceTest {

    @Autowired TimeSlotService slotService;
    @Autowired ServiceOfferingRepository services;
    @Autowired ResourceRepository resources;
    @Autowired TimeSlotRepository slots;

    private Long serviceId;
    private Long resourceId;

    @BeforeEach
    void setUp() {
        ServiceOffering s = services.save(ServiceOffering.builder().nom("Coupe").categorie(Category.COIFFURE)
                .dureeMinutes(30).prix(new BigDecimal("19")).capaciteDefaut(1).build());
        Resource r = resources.save(Resource.builder().nom("Karim").type("COIFFEUR").categorie(Category.COIFFURE).build());
        serviceId = s.getId();
        resourceId = r.getId();
    }

    @Test
    @DisplayName("Création OK sans chevauchement")
    void creationOk() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        TimeSlot slot = slotService.createSlot(serviceId, resourceId, debut, debut.plusMinutes(30), 1);
        assertThat(slot.getId()).isNotNull();
        assertThat(slot.getStatut()).isEqualTo(SlotStatus.OUVERT);
    }

    @Test
    @DisplayName("Chevauchement sur la même ressource refusé")
    void chevauchementRefuse() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        slotService.createSlot(serviceId, resourceId, debut, debut.plusMinutes(30), 1);

        assertThatThrownBy(() -> slotService.createSlot(serviceId, resourceId,
                debut.plusMinutes(15), debut.plusMinutes(45), 1))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("Conflit");
    }

    @Test
    @DisplayName("Créneaux adjacents (fin == début) autorisés")
    void creneauxAdjacentsOk() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        slotService.createSlot(serviceId, resourceId, debut, debut.plusMinutes(30), 1);
        TimeSlot suivant = slotService.createSlot(serviceId, resourceId,
                debut.plusMinutes(30), debut.plusMinutes(60), 1);
        assertThat(suivant.getId()).isNotNull();
    }

    @Test
    @DisplayName("Fin avant début refusée")
    void datesInvalides() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> slotService.createSlot(serviceId, resourceId, debut, debut.minusHours(1), 1))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    @DisplayName("Créneau dans le passé refusé")
    void passeRefuse() {
        LocalDateTime passe = LocalDateTime.now().minusDays(1);
        assertThatThrownBy(() -> slotService.createSlot(serviceId, resourceId, passe, passe.plusMinutes(30), 1))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    @DisplayName("Annuler puis rouvrir un créneau")
    void annulerPuisRouvrir() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        TimeSlot slot = slotService.createSlot(serviceId, resourceId, debut, debut.plusMinutes(30), 1);

        assertThat(slotService.cancelSlot(slot.getId()).getStatut()).isEqualTo(SlotStatus.ANNULE);
        assertThat(slotService.reopenSlot(slot.getId()).getStatut()).isEqualTo(SlotStatus.OUVERT);
    }

    @Test
    @DisplayName("Rouvrir un créneau plein le laisse COMPLET")
    void rouvrirCompletResteComplet() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        TimeSlot slot = slotService.createSlot(serviceId, resourceId, debut, debut.plusMinutes(30), 1);
        slot.setPlacesReservees(1);
        slots.save(slot);

        assertThat(slotService.reopenSlot(slot.getId()).getStatut()).isEqualTo(SlotStatus.COMPLET);
    }

    @Test
    @DisplayName("Annuler un créneau inexistant")
    void annulerInexistant() {
        assertThatThrownBy(() -> slotService.cancelSlot(999999L))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    @DisplayName("Lister les disponibilités")
    void listerDisponibles() {
        // Contexte partagé avec DataInitializer : on vérifie la présence, pas le total.
        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        slotService.createSlot(serviceId, resourceId, debut, debut.plusMinutes(30), 1);
        slotService.createSlot(serviceId, resourceId, debut.plusMinutes(30), debut.plusMinutes(60), 1);

        assertThat(slotService.listAvailable(Category.COIFFURE))
                .extracting(TimeSlot::getDebut)
                .contains(debut, debut.plusMinutes(30));
        assertThat(slotService.listAll())
                .extracting(TimeSlot::getDebut)
                .contains(debut, debut.plusMinutes(30));
    }
}
