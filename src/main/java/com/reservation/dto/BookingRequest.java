package com.reservation.dto;

import jakarta.validation.constraints.*;

public class BookingRequest {
    @NotNull(message = "Le créneau est obligatoire")
    private Long slotId;

    @Min(value = 1, message = "Il faut au moins 1 place")
    private int nbPlaces = 1;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    private String telephone;

    public BookingRequest() {}
    public BookingRequest(Long slotId, int nbPlaces, String nom, String email, String telephone) {
        this.slotId = slotId; this.nbPlaces = nbPlaces; this.nom = nom;
        this.email = email; this.telephone = telephone;
    }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long s) { this.slotId = s; }
    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int n) { this.nbPlaces = n; }
    public String getNom() { return nom; }
    public void setNom(String n) { this.nom = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String t) { this.telephone = t; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long slotId; private int nbPlaces = 1; private String nom; private String email; private String telephone;
        public Builder slotId(Long v) { slotId = v; return this; }
        public Builder nbPlaces(int v) { nbPlaces = v; return this; }
        public Builder nom(String v) { nom = v; return this; }
        public Builder email(String v) { email = v; return this; }
        public Builder telephone(String v) { telephone = v; return this; }
        public BookingRequest build() { return new BookingRequest(slotId, nbPlaces, nom, email, telephone); }
    }
}
