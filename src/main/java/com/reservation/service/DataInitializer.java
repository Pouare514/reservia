package com.reservation.service;

import com.reservation.model.*;
import com.reservation.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Jeu de données de démo : 3 univers (cinéma, hôtel, coiffure). */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ServiceOfferingRepository services;
    private final ResourceRepository resources;
    private final TimeSlotRepository slots;
    private final CustomerRepository customers;

    public DataInitializer(ServiceOfferingRepository services, ResourceRepository resources,
                           TimeSlotRepository slots, CustomerRepository customers) {
        this.services = services;
        this.resources = resources;
        this.slots = slots;
        this.customers = customers;
    }

    @Override
    public void run(String... args) {
        if (services.count() > 0) return;

        ServiceOffering dune = services.save(ServiceOffering.builder()
                .nom("Dune : Deuxième partie").categorie(Category.CINEMA)
                .description("Science-fiction épique — 2h46, salle 4K Dolby Atmos.")
                .dureeMinutes(166).prix(new BigDecimal("9.50")).capaciteDefaut(80).icone("🎬").build());
        ServiceOffering concert = services.save(ServiceOffering.builder()
                .nom("Concert Symphonique").categorie(Category.CINEMA)
                .description("Orchestre philharmonique — soirée exceptionnelle.")
                .dureeMinutes(120).prix(new BigDecimal("24.00")).capaciteDefaut(120).icone("🎶").build());
        ServiceOffering nuit = services.save(ServiceOffering.builder()
                .nom("Nuitée Chambre Double").categorie(Category.HOTEL)
                .description("Chambre double, petit-déjeuner inclus, vue jardin.")
                .dureeMinutes(1440).prix(new BigDecimal("89.00")).capaciteDefaut(2).icone("🏨").build());
        ServiceOffering suite = services.save(ServiceOffering.builder()
                .nom("Nuitée Suite Familiale").categorie(Category.HOTEL)
                .description("Suite 4 personnes, balcon, petit-déjeuner inclus.")
                .dureeMinutes(1440).prix(new BigDecimal("149.00")).capaciteDefaut(4).icone("🛏️").build());
        ServiceOffering coupe = services.save(ServiceOffering.builder()
                .nom("Coupe Homme").categorie(Category.COIFFURE)
                .description("Coupe + finition, 30 min avec Karim.")
                .dureeMinutes(30).prix(new BigDecimal("19.00")).capaciteDefaut(1).icone("💈").build());
        ServiceOffering couleur = services.save(ServiceOffering.builder()
                .nom("Couleur + Balayage").categorie(Category.COIFFURE)
                .description("Couleur complète + soin, 90 min avec Sofia.")
                .dureeMinutes(90).prix(new BigDecimal("65.00")).capaciteDefaut(1).icone("💇").build());

        Resource salle1 = resources.save(Resource.builder().nom("Salle 1 — Écran Géant").type("SALLE").categorie(Category.CINEMA).capacite(120).localisation("Niveau 1").build());
        Resource salle2 = resources.save(Resource.builder().nom("Salle 2 — Club").type("SALLE").categorie(Category.CINEMA).capacite(80).localisation("Niveau 2").build());
        Resource ch101 = resources.save(Resource.builder().nom("Chambre 101").type("CHAMBRE").categorie(Category.HOTEL).capacite(2).localisation("Étage 1").build());
        Resource ch102 = resources.save(Resource.builder().nom("Chambre 102").type("CHAMBRE").categorie(Category.HOTEL).capacite(2).localisation("Étage 1").build());
        Resource suiteF = resources.save(Resource.builder().nom("Suite Familiale").type("SUITE").categorie(Category.HOTEL).capacite(4).localisation("Étage 2").build());
        Resource karim = resources.save(Resource.builder().nom("Karim").type("COIFFEUR").categorie(Category.COIFFURE).capacite(1).localisation("Fauteuil A").build());
        Resource sofia = resources.save(Resource.builder().nom("Sofia").type("COIFFEUSE").categorie(Category.COIFFURE).capacite(1).localisation("Fauteuil B").build());

        LocalDate demain = LocalDate.now().plusDays(1);
        LocalDate j2 = LocalDate.now().plusDays(2);
        for (LocalDate d : List.of(demain, j2, LocalDate.now().plusDays(3))) {
            slots.save(slot(dune, salle1, LocalDateTime.of(d, LocalTime.of(14, 30)), 166, 80));
            slots.save(slot(dune, salle1, LocalDateTime.of(d, LocalTime.of(20, 30)), 166, 80));
            slots.save(slot(concert, salle2, LocalDateTime.of(d, LocalTime.of(19, 0)), 120, 60));
        }
        for (LocalDate d : List.of(demain, j2)) {
            slots.save(slot(nuit, ch101, LocalDateTime.of(d, LocalTime.of(15, 0)), 20 * 60, 2));
            slots.save(slot(nuit, ch102, LocalDateTime.of(d, LocalTime.of(15, 0)), 20 * 60, 2));
            slots.save(slot(suite, suiteF, LocalDateTime.of(d, LocalTime.of(15, 0)), 20 * 60, 4));
        }
        for (LocalDate d : List.of(demain, j2)) {
            LocalDateTime t = LocalDateTime.of(d, LocalTime.of(9, 0));
            for (int i = 0; i < 6; i++) {
                slots.save(slot(coupe, karim, t.plusMinutes(i * 40L), 30, 1));
            }
            slots.save(slot(couleur, sofia, LocalDateTime.of(d, LocalTime.of(9, 30)), 90, 1));
            slots.save(slot(couleur, sofia, LocalDateTime.of(d, LocalTime.of(14, 0)), 90, 1));
        }

        customers.save(Customer.builder().nom("Alice Martin").email("alice@example.com").telephone("06 11 22 33 44").build());
    }

    private TimeSlot slot(ServiceOffering s, Resource r, LocalDateTime debut, int dureeMin, int places) {
        return TimeSlot.builder().service(s).resource(r)
                .debut(debut).fin(debut.plusMinutes(dureeMin))
                .placesTotales(places).placesReservees(0).statut(SlotStatus.OUVERT).build();
    }
}
