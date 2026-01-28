package org.sid.livraisonservice.services;

import lombok.RequiredArgsConstructor;
import org.sid.livraisonservice.entities.LigneLivraison;
import org.sid.livraisonservice.entities.Livraison;
import org.sid.livraisonservice.entities.Tournee;
import org.sid.livraisonservice.repositories.LigneLivraisonRepository;
import org.sid.livraisonservice.repositories.LivraisonRepository;
import org.sid.livraisonservice.repositories.TourneeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LigneLivraisonServiceImpl implements LigneLivraisonService {

    private final LigneLivraisonRepository ligneLivraisonRepository;
    private final LivraisonRepository livraisonRepository;
    private final TourneeRepository tourneeRepository;

    @Override
    public void updateQuantiteLivree(Long id, Double quantiteLivree) {
        LigneLivraison ligne = ligneLivraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec l'id " + id));

        ligne.setQuantiteLivree(quantiteLivree);
        ligneLivraisonRepository.save(ligne);

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("codeProduit", ligne.getCodeProduit());
            body.put("quantiteLivree", quantiteLivree);

            String url = "http://localhost:8090/api/commandes/" + ligne.getLigneCommandeId() + "/update-quantite-livree";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(url, body);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour dans commande-service : " + e.getMessage());
        }

        Livraison livraison = ligne.getLivraison();
        livraison.setStatut(StatutUtils.calculerStatutLivraison(livraison));
        livraisonRepository.save(livraison);

        //  LOG 1
        System.out.println("Livraison ID " + livraison.getId() + " - Nouveau statut : " + livraison.getStatut());

        Tournee tournee = livraison.getTournee();
        if (tournee != null) {
            tournee.setStatut(StatutUtils.calculerStatutTournee(tournee));
            tourneeRepository.save(tournee);

            //  LOG 2
            System.out.println("Tournée ID " + tournee.getId() + " - Nouveau statut : " + tournee.getStatut());
        }
    }


}