package org.sid.livraisonservice.services;

import org.sid.livraisonservice.dtos.LigneLivraisonReliquatDTO;
import org.sid.livraisonservice.dtos.LivraisonDTO;
import org.sid.livraisonservice.dtos.ReliquatAffectationDTO;
import org.sid.livraisonservice.dtos.TourneeDTO;

import java.util.List;

public interface LivraisonService {
    LivraisonDTO save(LivraisonDTO dto);
    List<LivraisonDTO> findAll();
    LivraisonDTO findById(Long id);
    List<TourneeDTO> findTourneesWithLivraisons();
    String generateNextReferenceTournee();

    List<LivraisonDTO> findByChauffeurMatricule(String matricule);

    List<LigneLivraisonReliquatDTO> getLignesAvecReliquats();
    void affecterReliquat(ReliquatAffectationDTO dto);


}