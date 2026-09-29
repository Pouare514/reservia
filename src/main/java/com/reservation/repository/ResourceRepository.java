package com.reservation.repository;

import com.reservation.model.Category;
import com.reservation.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByCategorie(Category categorie);
}
