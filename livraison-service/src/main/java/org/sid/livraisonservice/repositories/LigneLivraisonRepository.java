package org.sid.livraisonservice.repositories;

import org.sid.livraisonservice.entities.LigneLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneLivraisonRepository extends JpaRepository<LigneLivraison, Long> {
}

