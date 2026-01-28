package org.sid.livraisonservice.repositories;

import org.sid.livraisonservice.entities.Tournee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourneeRepository extends JpaRepository<Tournee, Long> {
}
