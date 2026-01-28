package org.sid.camionservice.services;

import lombok.RequiredArgsConstructor;
import org.sid.camionservice.dtos.ChauffeurDTO;
import org.sid.camionservice.entities.Chauffeur;
import org.sid.camionservice.repositories.ChauffeurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChauffeurService {

    private final ChauffeurRepository chauffeurRepository;

    public Chauffeur save(ChauffeurDTO dto) {
        Chauffeur chauffeur = new Chauffeur();
        chauffeur.setMatricule(dto.getMatricule());
        chauffeur.setNom(dto.getNom());
        chauffeur.setPrenom(dto.getPrenom());
        chauffeur.setTelephone(dto.getTelephone());
        chauffeur.setEmail(dto.getEmail());
        chauffeur.setUserId(dto.getUserId()); // ✅ lier au user
        return chauffeurRepository.save(chauffeur);
    }


    public List<Chauffeur> getAll() {
        return chauffeurRepository.findAll();
    }

    public Chauffeur getByMatricule(String matricule) {
        return chauffeurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé : " + matricule));
    }

    public Chauffeur getByUserId(Long userId) {
        return chauffeurRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé pour l'utilisateur " + userId));
    }

    public Chauffeur update(Long id, ChauffeurDTO dto) {
        Chauffeur chauffeur = chauffeurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec l'id : " + id));

        chauffeur.setMatricule(dto.getMatricule());
        chauffeur.setNom(dto.getNom());
        chauffeur.setPrenom(dto.getPrenom());
        chauffeur.setTelephone(dto.getTelephone());
        chauffeur.setEmail(dto.getEmail());
        chauffeur.setUserId(dto.getUserId());

        return chauffeurRepository.save(chauffeur);
    }

    public void delete(Long id) {
        Chauffeur chauffeur = chauffeurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec l'id : " + id));
        chauffeurRepository.delete(chauffeur);
    }


}
