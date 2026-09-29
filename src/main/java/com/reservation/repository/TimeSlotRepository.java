package com.reservation.repository;

import com.reservation.model.TimeSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /** Verrou pessimiste : indispensable pour éviter les doubles réservations concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TimeSlot s where s.id = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select s from TimeSlot s
        join fetch s.service join fetch s.resource
        where s.statut = com.reservation.model.SlotStatus.OUVERT
          and s.debut > CURRENT_TIMESTAMP
          and (:categorie is null or s.service.categorie = :categorie)
        order by s.debut asc
        """)
    List<TimeSlot> findAvailable(@Param("categorie") com.reservation.model.Category categorie);

    @Query("""
        select s from TimeSlot s
        where s.resource.id = :resourceId
          and s.statut <> com.reservation.model.SlotStatus.ANNULE
          and s.debut < :fin and s.fin > :debut
          and (:excludeId is null or s.id <> :excludeId)
        """)
    List<TimeSlot> findOverlapping(@Param("resourceId") Long resourceId,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin,
                                   @Param("excludeId") Long excludeId);

    default List<TimeSlot> findOverlapping(Long resourceId, LocalDateTime debut, LocalDateTime fin) {
        return findOverlapping(resourceId, debut, fin, null);
    }

    @Query("select count(s) from TimeSlot s where s.statut = com.reservation.model.SlotStatus.OUVERT and s.debut > CURRENT_TIMESTAMP")
    long countAvailable();
}
