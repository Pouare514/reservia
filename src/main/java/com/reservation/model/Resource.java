package com.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "resources")
public class Resource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la ressource est obligatoire")
    private String nom;

    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category categorie;

    @Min(1)
    private int capacite = 1;

    private String localisation;

    public Resource() {}
    public Resource(Long id, String nom, String type, Category categorie, int capacite, String localisation) {
        this.id = id; this.nom = nom; this.type = type; this.categorie = categorie;
        this.capacite = capacite; this.localisation = localisation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String n) { this.nom = n; }
    public String getType() { return type; }
    public void setType(String t) { this.type = t; }
    public Category getCategorie() { return categorie; }
    public void setCategorie(Category c) { this.categorie = c; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int c) { this.capacite = c; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String l) { this.localisation = l; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String nom; private String type; private Category categorie;
        private int capacite = 1; private String localisation;
        public Builder id(Long v) { id = v; return this; }
        public Builder nom(String v) { nom = v; return this; }
        public Builder type(String v) { type = v; return this; }
        public Builder categorie(Category v) { categorie = v; return this; }
        public Builder capacite(int v) { capacite = v; return this; }
        public Builder localisation(String v) { localisation = v; return this; }
        public Resource build() { return new Resource(id, nom, type, categorie, capacite, localisation); }
    }
}
