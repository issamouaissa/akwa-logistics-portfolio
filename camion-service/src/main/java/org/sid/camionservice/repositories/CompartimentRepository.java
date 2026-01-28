package org.sid.camionservice.repositories;

import org.sid.camionservice.entities.Compartiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompartimentRepository extends JpaRepository<Compartiment, Long> {
}