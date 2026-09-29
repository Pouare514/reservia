package com.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    private String telephone;

    public Customer() {}
    public Customer(Long id, String nom, String email, String telephone) {
        this.id = id; this.nom = nom; this.email = email; this.telephone = telephone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String nom; private String email; private String telephone;
        public Builder id(Long v) { id = v; return this; }
        public Builder nom(String v) { nom = v; return this; }
        public Builder email(String v) { email = v; return this; }
        public Builder telephone(String v) { telephone = v; return this; }
        public Customer build() { return new Customer(id, nom, email, telephone); }
    }
}
