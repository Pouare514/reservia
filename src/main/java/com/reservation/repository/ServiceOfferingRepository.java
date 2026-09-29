package com.reservation.repository;

import com.reservation.model.Category;
import com.reservation.model.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    List<ServiceOffering> findByCategorie(Category categorie);
}
