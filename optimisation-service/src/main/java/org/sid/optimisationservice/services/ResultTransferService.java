package org.sid.optimisationservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResultTransferService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String RESULT_FILE_PATH = "resultats_affectation.json";
    private static final String LIVRAISON_SERVICE_URL = "";
    private static final String COMMANDE_SERVICE_URL = "";
    private static final String CAMION_SERVICE_URL = "";

    public String transferResultsToLivraisonService() {
        try {
            File resultFile = new File(RESULT_FILE_PATH);
            if (!resultFile.exists()) return "Aucun résultat d’optimisation trouvé.";

            List<Map<String, Object>> resultList = objectMapper.readValue(resultFile, new TypeReference<>() {});

            for (Map<String, Object> assignment : resultList) {

                String camionMatricule = (String) assignment.get("Camion");
                ResponseEntity<Map> camionResponse = restTemplate.getForEntity(
                        CAMION_SERVICE_URL + camionMatricule, Map.class
                );
                if (!camionResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Camion non trouvé : " + camionMatricule);
                    continue;
                }

                Map<String, Object> camionData = camionResponse.getBody();
                Map<String, Object> chauffeur = (Map<String, Object>) camionData.get("chauffeur");
                Map<String, Object> depot = (Map<String, Object>) camionData.get("depot");

                Map<String, Object> livraisonDto = new LinkedHashMap<>();
                livraisonDto.put("dateLivraison", new Date());
                livraisonDto.put("camionMatricule", camionMatricule);
                livraisonDto.put("statut", "EN_COURS");

                // Tournee enrichie
                String nextRef = restTemplate.getForObject(
                        "", String.class);

                livraisonDto.put("referenceTournee", nextRef);

                Map<String, Object> tournee = new LinkedHashMap<>();
                tournee.put("referenceTournee", nextRef);
                tournee.put("dateTournee", new Date());
                tournee.put("statut", "EN_COURS");
                tournee.put("camion", camionMatricule);
                tournee.put("chauffeurMatricule", chauffeur.get("matricule"));
                tournee.put("depot", depot.get("code"));

                livraisonDto.put("tournee", tournee);

                // 🧠 FUSION des lignes par codeProduit + ligneCommandeId
                Map<String, Map<String, Object>> lignesFusionnees = new LinkedHashMap<>();

                List<Map<String, Object>> commandes = (List<Map<String, Object>>) assignment.get("Commandes");
                Map<String, Map<String, Double>> compartiments =
                        (Map<String, Map<String, Double>>) assignment.get("Allocation_Compartiments");

                for (Map<String, Object> cmd : commandes) {
                    Long commandeId = Long.valueOf(cmd.get("CommandeID").toString());
                    List<Map<String, Object>> produits = (List<Map<String, Object>>) cmd.get("Produits");

                    for (Map<String, Object> produit : produits) {
                        Long codeProduit = Long.valueOf(produit.get("ProduitCode").toString());
                        Double qDemandee = ((Number) produit.get("quantiteDemandee")).doubleValue();
                        Double qProgrammee = ((Number) produit.get("quantiteProgrammee")).doubleValue();

                        String type = produit.containsKey("Quantite_DG") ? "gasoil" : "essence";
                        Map<String, Double> compartimentMap = compartiments.getOrDefault(type, Map.of());

                        String key = codeProduit + "_" + commandeId;
                        Map<String, Object> ligneLivraison = lignesFusionnees.getOrDefault(key, new LinkedHashMap<>());

                        // Première fois → créer structure ligne
                        if (!ligneLivraison.containsKey("codeProduit")) {
                            ligneLivraison.put("codeProduit", codeProduit);
                            ligneLivraison.put("quantiteDemandee", qDemandee);
                            ligneLivraison.put("quantiteProgrammee", qProgrammee);
                            ligneLivraison.put("quantiteLivree", null);
                            String ligneUrl = COMMANDE_SERVICE_URL + "/lignes-commandes/by-commande-produit?commandeId=" + commandeId + "&codeProduit=" + codeProduit;
                            try {
                                ResponseEntity<Map> ligneResponse = restTemplate.getForEntity(ligneUrl, Map.class);
                                if (ligneResponse.getStatusCode().is2xxSuccessful()) {
                                    Map<String, Object> ligneCmd = ligneResponse.getBody();

                                    if (ligneCmd != null && ligneCmd.get("prixUnitaire") != null) {
                                        ligneLivraison.put("prixUnitaire", Double.parseDouble(ligneCmd.get("prixUnitaire").toString()));
                                    } else {
                                        ligneLivraison.put("prixUnitaire", null); // fallback
                                    }
                                } else {
                                    ligneLivraison.put("prixUnitaire", null);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur récupération prixUnitaire : " + e.getMessage());
                                ligneLivraison.put("prixUnitaire", null);
                            }

                            ligneLivraison.put("ligneCommandeId", commandeId);
                            ligneLivraison.put("detailsCompartiments", new ArrayList<Map<String, Object>>());
                        }

                        List<Map<String, Object>> detailsCompartiments =
                                (List<Map<String, Object>>) ligneLivraison.get("detailsCompartiments");

                        for (Map.Entry<String, Double> entry : compartimentMap.entrySet()) {
                            Long compartiment = Long.parseLong(entry.getKey());
                            Double qPartielle = entry.getValue();

                            Map<String, Object> detail = new HashMap<>();
                            detail.put("compartiment", compartiment);
                            detail.put("capaciteCompartiment", qPartielle);
                            detail.put("quantiteDemandee", qDemandee);
                            detail.put("quantiteProgrammee", qProgrammee);
                            detailsCompartiments.add(detail);
                        }

                        lignesFusionnees.put(key, ligneLivraison);

                        // Mise à jour côté commande
                        Map<String, Object> updatePayload = new HashMap<>();
                        updatePayload.put("codeProduit", codeProduit);
                        updatePayload.put("quantiteProgrammee", qProgrammee);
                        restTemplate.put(COMMANDE_SERVICE_URL + "/" + commandeId + "/update-ligne", updatePayload);
                    }
                }

                // Finaliser les lignes de livraison
                livraisonDto.put("lignesLivraison", new ArrayList<>(lignesFusionnees.values()));

                // Envoi au service de livraison
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(livraisonDto);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        LIVRAISON_SERVICE_URL, request, String.class);

                System.out.println("Livraison créée avec tournée : " + response.getBody());
            }

            return "Transfert terminé avec succès.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur durant le transfert : " + e.getMessage();
        }
    }
}