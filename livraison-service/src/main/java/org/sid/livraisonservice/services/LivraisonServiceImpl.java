package org.sid.livraisonservice.services;

import lombok.RequiredArgsConstructor;
import org.sid.livraisonservice.dtos.*;
import org.sid.livraisonservice.entities.LigneLivraison;
import org.sid.livraisonservice.entities.Livraison;
import org.sid.livraisonservice.entities.Tournee;
import org.sid.livraisonservice.mappers.LivraisonMapper;
import org.sid.livraisonservice.repositories.LigneLivraisonRepository;
import org.sid.livraisonservice.repositories.LivraisonRepository;
import org.sid.livraisonservice.repositories.TourneeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LivraisonServiceImpl implements LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final LivraisonMapper mapper;
    private final TourneeRepository tourneeRepository;
    private final LigneLivraisonRepository ligneLivraisonRepository;


    @Override
    public LivraisonDTO save(LivraisonDTO dto) {
        Livraison entity = mapper.toEntity(dto);

        //  Calcul dynamique du statut de la livraison
        entity.setStatut(StatutUtils.calculerStatutLivraison(entity));

        //  Si une tournée est associée, recalcul du statut de la tournée
        if (entity.getTournee() != null) {
            Tournee tournee = entity.getTournee();

            // Très important : on ajoute la livraison dans la tournée AVANT de recalculer le statut
            if (!tournee.getLivraisons().contains(entity)) {
                tournee.getLivraisons().add(entity);
            }

            tournee.setStatut(StatutUtils.calculerStatutTournee(tournee));
        }

        //  Sauvegarde
        Livraison saved = livraisonRepository.save(entity);
        return mapper.toDTO(saved);
    }


    @Override
    public List<LivraisonDTO> findAll() {
        return livraisonRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public LivraisonDTO findById(Long id) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));
        return mapper.toDTO(livraison);
    }

    @Override
    public List<TourneeDTO> findTourneesWithLivraisons() {
        return tourneeRepository.findAll().stream()
                .map(mapper::toTourneeDTOWithLivraisons)
                .collect(Collectors.toList());
    }

    @Override
    public String generateNextReferenceTournee() {
        List<String> references = tourneeRepository.findAll().stream()
                .map(Tournee::getReferenceTournee)
                .filter(ref -> ref != null && ref.matches("\\d{8}"))
                .sorted()
                .toList();

        String lastRef = references.isEmpty() ? "63704310" : references.get(references.size() - 1);
        int nextInt = Integer.parseInt(lastRef) + 1;

        return String.format("%08d", nextInt);
    }


    @Override
    public List<LivraisonDTO> findByChauffeurMatricule(String matricule) {
        List<Livraison> livraisons = livraisonRepository.findByTournee_ChauffeurMatricule(matricule);
        return livraisons.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<LigneLivraisonReliquatDTO> getLignesAvecReliquats() {
        RestTemplate restTemplate = new RestTemplate();

        return ligneLivraisonRepository.findAll().stream()
                .filter(l -> l.getQuantiteLivree() != null && l.getQuantiteProgrammee() != null)
                .filter(l -> l.getQuantiteLivree() < l.getQuantiteProgrammee())
                .map(l -> {
                    LigneLivraisonReliquatDTO dto = new LigneLivraisonReliquatDTO();
                    dto.setLigneLivraisonId(l.getId());
                    dto.setCodeProduit(l.getCodeProduit());
                    dto.setQuantiteRestante(l.getQuantiteProgrammee() - l.getQuantiteLivree());
                    dto.setLigneCommandeId(l.getLigneCommandeId());

                    // 🔁 appel à commande-service pour récupérer la commande
                    try {
                        String urlCommande = "http://localhost:8090/api/commandes/" + l.getLigneCommandeId();
                        CommandeDTO commande = restTemplate.getForObject(urlCommande, CommandeDTO.class);
                        if (commande != null) {
                            dto.setReferenceCommande(commande.getReferenceCommande());
                            dto.setStationInitialeId(commande.getStationId());
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur lors de l'appel à commande-service : " + e.getMessage());
                    }

                    return dto;
                }).collect(Collectors.toList());
    }



    @Override
    public void affecterReliquat(ReliquatAffectationDTO dto) {
        // 1. Vérifier que la ligne de livraison existe
        LigneLivraison ligne = ligneLivraisonRepository.findById(dto.getLigneLivraisonId())
                .orElseThrow(() -> new RuntimeException("Ligne de livraison non trouvée"));

        // 2. Créer une ligne de commande manuellement (via appel REST au commande-service)
        Map<String, Object> ligneCommande = new HashMap<>();
        ligneCommande.put("codeProduit", ligne.getCodeProduit());
        ligneCommande.put("quantiteDemandee", dto.getQuantiteAAffecter());
        ligneCommande.put("stationId", dto.getNouvelleStationId());

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity("http://localhost:8090/api/commandes", ligneCommande, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Erreur d'affectation : " + e.getMessage());
        }
    }
}