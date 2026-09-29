package com.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Locale;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReservationApplication {
    public static void main(String[] args) {
        // Dates Thymeleaf en français ("mer. 30 sept.") au lieu d'en anglais.
        Locale.setDefault(Locale.FRANCE);
        SpringApplication.run(ReservationApplication.class, args);
    }
}
