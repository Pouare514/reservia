package com.reservation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private ServiceOffering service;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false)
    private int placesTotales;

    private int placesReservees = 0;

    @Enumerated(EnumType.STRING)
    private SlotStatus statut = SlotStatus.OUVERT;

    @Version
    private Long version;

    public TimeSlot() {}
    public TimeSlot(Long id, ServiceOffering service, Resource resource, LocalDateTime debut,
                    LocalDateTime fin, int placesTotales, int placesReservees, SlotStatus statut) {
        this.id = id; this.service = service; this.resource = resource;
        this.debut = debut; this.fin = fin;
        this.placesTotales = placesTotales; this.placesReservees = placesReservees;
        this.statut = statut;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ServiceOffering getService() { return service; }
    public void setService(ServiceOffering s) { this.service = s; }
    public Resource getResource() { return resource; }
    public void setResource(Resource r) { this.resource = r; }
    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime d) { this.debut = d; }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime f) { this.fin = f; }
    public int getPlacesTotales() { return placesTotales; }
    public void setPlacesTotales(int p) { this.placesTotales = p; }
    public int getPlacesReservees() { return placesReservees; }
    public void setPlacesReservees(int p) { this.placesReservees = p; }
    public SlotStatus getStatut() { return statut; }
    public void setStatut(SlotStatus s) { this.statut = s; }
    public Long getVersion() { return version; }

    @Transient
    public int getPlacesDisponibles() {
        return Math.max(0, placesTotales - placesReservees);
    }

    @Transient
    public boolean isComplet() {
        return statut == SlotStatus.COMPLET || getPlacesDisponibles() <= 0;
    }

    @Transient
    public boolean isPasse() {
        return debut.isBefore(LocalDateTime.now());
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private ServiceOffering service; private Resource resource;
        private LocalDateTime debut; private LocalDateTime fin;
        private int placesTotales; private int placesReservees = 0; private SlotStatus statut = SlotStatus.OUVERT;
        public Builder id(Long v) { id = v; return this; }
        public Builder service(ServiceOffering v) { service = v; return this; }
        public Builder resource(Resource v) { resource = v; return this; }
        public Builder debut(LocalDateTime v) { debut = v; return this; }
        public Builder fin(LocalDateTime v) { fin = v; return this; }
        public Builder placesTotales(int v) { placesTotales = v; return this; }
        public Builder placesReservees(int v) { placesReservees = v; return this; }
        public Builder statut(SlotStatus v) { statut = v; return this; }
        public TimeSlot build() {
            return new TimeSlot(id, service, resource, debut, fin, placesTotales, placesReservees, statut);
        }
    }
}
