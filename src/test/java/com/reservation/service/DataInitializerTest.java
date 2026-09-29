package com.reservation.service;

import com.reservation.repository.CustomerRepository;
import com.reservation.repository.ServiceOfferingRepository;
import com.reservation.repository.TimeSlotRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

/** Le jeu de démo (DataInitializer) charge 6 services et des créneaux futurs. */
@SpringBootTest
class DataInitializerTest {

    @Autowired ServiceOfferingRepository services;
    @Autowired TimeSlotRepository slots;
    @Autowired CustomerRepository customers;

    @Test
    @DisplayName("Données de démo présentes au démarrage")
    void jeuDeDemoCharge() {
        assertThat(services.count()).isEqualTo(6);
        assertThat(slots.count()).isGreaterThan(20);
        assertThat(customers.findByEmailIgnoreCase("alice@example.com")).isPresent();
    }
}
