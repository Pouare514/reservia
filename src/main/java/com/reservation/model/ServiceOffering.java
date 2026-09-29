package com.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class ServiceOffering {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du service est obligatoire")
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category categorie;

    @Column(length = 1000)
    private String description;

    @Min(value = 1, message = "Durée invalide")
    private int dureeMinutes;

    @DecimalMin(value = "0.0", message = "Prix invalide")
    @Column(precision = 10, scale = 2)
    private BigDecimal prix;

    @Min(value = 1, message = "Capacité invalide")
    private int capaciteDefaut = 1;

    private String icone;

    public ServiceOffering() {}
    public ServiceOffering(Long id, String nom, Category categorie, String description,
                           int dureeMinutes, BigDecimal prix, int capaciteDefaut, String icone) {
        this.id = id; this.nom = nom; this.categorie = categorie; this.description = description;
        this.dureeMinutes = dureeMinutes; this.prix = prix; this.capaciteDefaut = capaciteDefaut;
        this.icone = icone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Category getCategorie() { return categorie; }
    public void setCategorie(Category c) { this.categorie = c; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int d) { this.dureeMinutes = d; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal p) { this.prix = p; }
    public int getCapaciteDefaut() { return capaciteDefaut; }
    public void setCapaciteDefaut(int c) { this.capaciteDefaut = c; }
    public String getIcone() { return icone; }
    public void setIcone(String i) { this.icone = i; }

    /** Durée lisible : "30 min", "2h00", "2h46", "24h00". Évite les divisions flottantes en template. */
    @Transient
    public String getDureeFormatee() {
        if (dureeMinutes < 60) return dureeMinutes + " min";
        int h = dureeMinutes / 60, r = dureeMinutes % 60;
        return r == 0 ? h + "h00" : h + "h" + String.format("%02d", r);
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String nom; private Category categorie; private String description;
        private int dureeMinutes; private BigDecimal prix; private int capaciteDefaut = 1; private String icone;
        public Builder id(Long v) { id = v; return this; }
        public Builder nom(String v) { nom = v; return this; }
        public Builder categorie(Category v) { categorie = v; return this; }
        public Builder description(String v) { description = v; return this; }
        public Builder dureeMinutes(int v) { dureeMinutes = v; return this; }
        public Builder prix(BigDecimal v) { prix = v; return this; }
        public Builder capaciteDefaut(int v) { capaciteDefaut = v; return this; }
        public Builder icone(String v) { icone = v; return this; }
        public ServiceOffering build() {
            return new ServiceOffering(id, nom, categorie, description, dureeMinutes, prix, capaciteDefaut, icone);
        }
    }
}
