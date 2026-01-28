package org.sid.camionservice.repositories;

import org.sid.camionservice.entities.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    Optional<Depot> findByCode(String code);
}

