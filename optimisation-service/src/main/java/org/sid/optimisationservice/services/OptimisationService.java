package org.sid.optimisationservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OptimisationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<Long, Object> getCommandes() {
        String url = ""; // adapte si besoin

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> commandesList = response.getBody();

        return commandesList.stream().collect(Collectors.toMap(
                cmd -> Long.valueOf(cmd.get("id").toString()),
                cmd -> cmd
        ));
    }

    public Map<String, Object> getCamions() {
        String url = ""; // adapte si besoin

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> camionsList = response.getBody();

        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> camion : camionsList) {
            result.put(camion.get("matricule").toString(), camion);
        }
        return result;
    }



    public void lancerOptimisationAvecCommandes(List<Long> selectedCommandeIds) throws IOException {
        // 1. Appel API pour récupérer toutes les commandes
        Map<Long, Object> toutesCommandes = getCommandes();

        // 2. Filtrer les commandes sélectionnées
        Map<Long, Object> commandesFiltrees = toutesCommandes.entrySet().stream()
                .filter(entry -> selectedCommandeIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 3. Récupérer les camions disponibles
        Map<String, Object> camions = getCamions();

        // 4. Sauvegarder dans fichiers JSON
        ObjectMapper mapper = new ObjectMapper();

        // Chemin absolu cohérent avec ton script Python
        String basePath = "";
        File commandesFile = new File(basePath + "commandes_selectionnees.json");
        File camionsFile = new File(basePath + "camions_disponibles.json");


        File resultFile = new File(basePath + "resultats_affectation.json");
        if (resultFile.exists()) {
            resultFile.delete();  // ❌ évite la confusion avec un résultat ancien
        }

        // Écriture des nouvelles données
        mapper.writeValue(commandesFile, commandesFiltrees.values());
        mapper.writeValue(camionsFile, camions.values());

        // Log de contrôle
        System.out.println("Fichier commandes_selectionnees.json mis à jour avec les commandes : " + selectedCommandeIds);
        System.out.println("Nombre de camions disponibles : " + camions.size());

        // 5. Lancer le script Python
        ProcessBuilder pb = new ProcessBuilder(
                basePath + "venv\\Scripts\\python.exe", // Python venv
                "optimisation_script.py"                // Script Python
        );
        pb.directory(new File(basePath));      // Répertoire de travail
        pb.redirectErrorStream(true);          // Rediriger stderr vers stdout
        pb.inheritIO();                        // Afficher la sortie dans la console Java

        // Lancement
        Process process = pb.start();

        try {
            // MODIFICATION ICI : on entoure l'appel d'un bloc try-catch
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("L'exécution du script d'optimisation a échoué avec le code de sortie : " + exitCode);
            }
        } catch (InterruptedException e) {
            // Quand le processus est interrompu, on restaure le statut "interrompu" du thread
            Thread.currentThread().interrupt();
            // Et on lance une exception non-vérifiée pour signaler une erreur grave
            throw new RuntimeException("Le processus d'optimisation a été interrompu de manière inattendue.", e);
        }
    }

}