package org.sid.camionservice.repositories;

import org.sid.camionservice.entities.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
}
