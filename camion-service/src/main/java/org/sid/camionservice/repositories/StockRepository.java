package org.sid.camionservice.repositories;

import org.sid.camionservice.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByDepotId(Long depotId);
}
