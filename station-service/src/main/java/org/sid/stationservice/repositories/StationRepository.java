package org.sid.stationservice.repositories;

import org.sid.stationservice.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByCode(String code);
    List<Station> findByUserId(Long userId);

    boolean existsByCode(String code);


}