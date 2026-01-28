package org.sid.commandeservice.repositories;


import org.sid.commandeservice.entities.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {
    List<LigneCommande> findByCommandeId(Long commandeId);
    Optional<LigneCommande> findByCommandeIdAndCodeProduit(Long commandeId, Long codeProduit);

}

