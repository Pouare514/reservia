package com.reservation.repository;

import com.reservation.model.Booking;
import com.reservation.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerEmailIgnoreCaseOrderByCreeLeDesc(String email);
    List<Booking> findByStatutOrderByCreeLeDesc(BookingStatus statut);
    List<Booking> findAllByOrderByCreeLeDesc();
    long countByStatut(BookingStatus statut);
}
