package com.reservation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id")
    private TimeSlot slot;

    @Column(nullable = false)
    private int nbPlaces;

    @Enumerated(EnumType.STRING)
    private BookingStatus statut = BookingStatus.CONFIRMEE;

    private LocalDateTime creeLe = LocalDateTime.now();

    private LocalDateTime annuleLe;

    public Booking() {}
    public Booking(Long id, String code, Customer customer, TimeSlot slot, int nbPlaces,
                   BookingStatus statut, LocalDateTime creeLe, LocalDateTime annuleLe) {
        this.id = id; this.code = code; this.customer = customer; this.slot = slot;
        this.nbPlaces = nbPlaces; this.statut = statut; this.creeLe = creeLe; this.annuleLe = annuleLe;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String c) { this.code = c; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer c) { this.customer = c; }
    public TimeSlot getSlot() { return slot; }
    public void setSlot(TimeSlot s) { this.slot = s; }
    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int n) { this.nbPlaces = n; }
    public BookingStatus getStatut() { return statut; }
    public void setStatut(BookingStatus s) { this.statut = s; }
    public LocalDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(LocalDateTime c) { this.creeLe = c; }
    public LocalDateTime getAnnuleLe() { return annuleLe; }
    public void setAnnuleLe(LocalDateTime a) { this.annuleLe = a; }

    @PrePersist
    void prePersist() {
        if (creeLe == null) creeLe = LocalDateTime.now();
        if (code == null) code = genererCode();
    }

    public static String genererCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String code; private Customer customer; private TimeSlot slot;
        private int nbPlaces; private BookingStatus statut = BookingStatus.CONFIRMEE;
        private LocalDateTime creeLe = LocalDateTime.now(); private LocalDateTime annuleLe;
        public Builder id(Long v) { id = v; return this; }
        public Builder code(String v) { code = v; return this; }
        public Builder customer(Customer v) { customer = v; return this; }
        public Builder slot(TimeSlot v) { slot = v; return this; }
        public Builder nbPlaces(int v) { nbPlaces = v; return this; }
        public Builder statut(BookingStatus v) { statut = v; return this; }
        public Builder creeLe(LocalDateTime v) { creeLe = v; return this; }
        public Builder annuleLe(LocalDateTime v) { annuleLe = v; return this; }
        public Booking build() {
            return new Booking(id, code, customer, slot, nbPlaces, statut, creeLe, annuleLe);
        }
    }
}
