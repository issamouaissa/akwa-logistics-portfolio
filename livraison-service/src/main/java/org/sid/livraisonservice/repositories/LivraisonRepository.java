package org.sid.livraisonservice.repositories;

import org.sid.livraisonservice.entities.Livraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivraisonRepository extends JpaRepository<Livraison, Long> {

    List<Livraison> findByTournee_ChauffeurMatricule(String matricule);
}