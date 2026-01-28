package org.sid.commandeservice.repositories;


import org.sid.commandeservice.entities.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByUserId(Long userId);
    Optional<Commande> findByReferenceCommande(String referenceCommande);
    @Query("SELECT c.referenceCommande FROM Commande c ORDER BY c.id DESC LIMIT 1")
    String findLastReferenceCommande();

}